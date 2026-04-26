package com.banking.insight.service.impl;

import com.banking.insight.domain.Anomaly;
import com.banking.insight.event.AnomalyDetectedEvent;
import com.banking.insight.repository.AnomalyRepository;
import com.banking.insight.service.AnomalyDetectionService;
import com.banking.insight.service.DataAggregationService;
import com.banking.insight.util.StatisticalCalculator;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Transactional
public class ZScoreAnomalyDetectionService implements AnomalyDetectionService {

    private static final Logger logger = LoggerFactory.getLogger(ZScoreAnomalyDetectionService.class);
    private static final BigDecimal Z_SCORE_THRESHOLD = new BigDecimal("2.0");

    private final AnomalyRepository anomalyRepository;
    private final DataAggregationService dataAggregationService;
    private final StatisticalCalculator statisticalCalculator;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ZScoreAnomalyDetectionService(
        final AnomalyRepository anomalyRepository,
        final DataAggregationService dataAggregationService,
        final StatisticalCalculator statisticalCalculator,
        final KafkaTemplate<String, Object> kafkaTemplate
    ) {
        this.anomalyRepository = anomalyRepository;
        this.dataAggregationService = dataAggregationService;
        this.statisticalCalculator = statisticalCalculator;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public List<Anomaly> detectAnomalies(final UUID userId) {
        logger.info("Detecting anomalies for user: {}", userId);

        final Instant endDate = Instant.now();
        final Instant startDate = endDate.minus(90, ChronoUnit.DAYS);

        final List<Map<String, Object>> transactions = dataAggregationService.getUserTransactions(userId, startDate, endDate);
        
        final List<BigDecimal> amounts = transactions.stream()
            .map(t -> new BigDecimal(t.get("amount").toString()))
            .toList();

        final BigDecimal mean = statisticalCalculator.calculateMean(amounts);
        final BigDecimal stdDev = statisticalCalculator.calculateStandardDeviation(amounts, mean);

        final List<Anomaly> anomalies = new ArrayList<>();

        for (final Map<String, Object> transaction : transactions) {
            final BigDecimal amount = new BigDecimal(transaction.get("amount").toString());
            final BigDecimal zScore = statisticalCalculator.calculateZScore(amount, mean, stdDev);

            if (zScore.abs().compareTo(Z_SCORE_THRESHOLD) > 0) {
                final Anomaly anomaly = createAnomaly(userId, transaction, amount, mean, zScore);
                anomalies.add(anomalyRepository.save(anomaly));
                publishAnomalyDetectedEvent(anomaly);
            }
        }

        logger.info("Detected {} anomalies for user: {}", anomalies.size(), userId);
        return anomalies;
    }

    @Override
    @Cacheable(value = "anomalies", key = "#userId + '-unacknowledged'")
    public List<Anomaly> getUnacknowledgedAnomalies(final UUID userId) {
        return anomalyRepository.findUnacknowledgedByUserId(userId);
    }

    @Override
    @Cacheable(value = "anomalies", key = "#userId + '-critical'")
    public List<Anomaly> getCriticalAnomalies(final UUID userId) {
        return anomalyRepository.findCriticalUnacknowledgedByUserId(userId);
    }

    @Override
    public Anomaly getAnomalyById(final UUID anomalyId, final UUID userId) {
        return anomalyRepository.findByIdAndNotDeleted(anomalyId)
            .filter(anomaly -> anomaly.getUserId().equals(userId))
            .orElseThrow(() -> new EntityNotFoundException("Anomaly not found"));
    }

    @Override
    @CacheEvict(value = "anomalies", key = "#userId + '-unacknowledged'")
    public Anomaly acknowledgeAnomaly(final UUID anomalyId, final UUID userId, final String notes) {
        final Anomaly anomaly = getAnomalyById(anomalyId, userId);
        anomaly.acknowledge(notes);
        return anomalyRepository.save(anomaly);
    }

    @Override
    @CacheEvict(value = "anomalies", key = "#userId + '-unacknowledged'")
    public Anomaly markAsFalsePositive(final UUID anomalyId, final UUID userId, final String notes) {
        final Anomaly anomaly = getAnomalyById(anomalyId, userId);
        anomaly.markAsFalsePositive(notes);
        return anomalyRepository.save(anomaly);
    }

    private Anomaly createAnomaly(
        final UUID userId,
        final Map<String, Object> transaction,
        final BigDecimal amount,
        final BigDecimal mean,
        final BigDecimal zScore
    ) {
        final BigDecimal deviation = amount.subtract(mean).divide(mean, 2, java.math.RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
        final Anomaly.Severity severity = determineSeverity(zScore);

        return Anomaly.builder()
            .userId(userId)
            .transactionId(UUID.fromString(transaction.get("id").toString()))
            .anomalyType(Anomaly.AnomalyType.UNUSUAL_AMOUNT)
            .severity(severity)
            .description(String.format("Unusual transaction amount detected: %s", amount))
            .detectedValue(amount)
            .expectedValue(mean)
            .deviationPercentage(deviation)
            .zScore(zScore)
            .category(transaction.get("category").toString())
            .merchantName(transaction.get("merchantName").toString())
            .detectionMethod(Anomaly.DetectionMethod.Z_SCORE)
            .confidenceScore(new BigDecimal("85.00"))
            .metadata(new HashMap<>())
            .build();
    }

    private Anomaly.Severity determineSeverity(final BigDecimal zScore) {
        final BigDecimal absZScore = zScore.abs();
        if (absZScore.compareTo(new BigDecimal("3.0")) >= 0) return Anomaly.Severity.CRITICAL;
        if (absZScore.compareTo(new BigDecimal("2.5")) >= 0) return Anomaly.Severity.HIGH;
        if (absZScore.compareTo(new BigDecimal("2.0")) >= 0) return Anomaly.Severity.MEDIUM;
        return Anomaly.Severity.LOW;
    }

    private void publishAnomalyDetectedEvent(final Anomaly anomaly) {
        final AnomalyDetectedEvent event = AnomalyDetectedEvent.from(
            anomaly.getId(),
            anomaly.getUserId(),
            anomaly.getTransactionId(),
            anomaly.getAnomalyType().name(),
            anomaly.getSeverity().name(),
            anomaly.getDescription(),
            anomaly.getDetectedValue(),
            anomaly.getExpectedValue(),
            anomaly.getDeviationPercentage(),
            anomaly.getZScore(),
            anomaly.getCategory(),
            anomaly.getMerchantName(),
            anomaly.getDetectionMethod().name(),
            anomaly.getConfidenceScore(),
            anomaly.getMetadata()
        );
        kafkaTemplate.send("anomaly-detected", event);
    }
}
