package com.banking.insight.service.impl;

import com.banking.insight.domain.SpendingPattern;
import com.banking.insight.event.PatternIdentifiedEvent;
import com.banking.insight.repository.SpendingPatternRepository;
import com.banking.insight.service.DataAggregationService;
import com.banking.insight.service.PatternAnalysisService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class StatisticalPatternAnalysisService implements PatternAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(StatisticalPatternAnalysisService.class);

    private final SpendingPatternRepository spendingPatternRepository;
    private final DataAggregationService dataAggregationService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public StatisticalPatternAnalysisService(
        final SpendingPatternRepository spendingPatternRepository,
        final DataAggregationService dataAggregationService,
        final KafkaTemplate<String, Object> kafkaTemplate
    ) {
        this.spendingPatternRepository = spendingPatternRepository;
        this.dataAggregationService = dataAggregationService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public List<SpendingPattern> analyzeSpendingPatterns(final UUID userId) {
        logger.info("Analyzing spending patterns for user: {}", userId);

        final Instant endDate = Instant.now();
        final Instant startDate = endDate.minus(90, ChronoUnit.DAYS);

        final List<Map<String, Object>> transactions = dataAggregationService.getUserTransactions(userId, startDate, endDate);
        
        final Map<String, List<Map<String, Object>>> categoryGroups = transactions.stream()
            .collect(Collectors.groupingBy(t -> t.get("category").toString()));

        final List<SpendingPattern> patterns = new ArrayList<>();

        for (final Map.Entry<String, List<Map<String, Object>>> entry : categoryGroups.entrySet()) {
            final String category = entry.getKey();
            final List<Map<String, Object>> categoryTransactions = entry.getValue();

            if (categoryTransactions.size() >= 3) {
                final SpendingPattern pattern = analyzeCategory(userId, category, categoryTransactions);
                patterns.add(spendingPatternRepository.save(pattern));
                publishPatternIdentifiedEvent(pattern);
            }
        }

        logger.info("Identified {} spending patterns for user: {}", patterns.size(), userId);
        return patterns;
    }

    @Override
    @Cacheable(value = "patterns", key = "#userId + '-recurring'")
    public List<SpendingPattern> getRecurringPatterns(final UUID userId) {
        return spendingPatternRepository.findRecurringByUserId(userId);
    }

    @Override
    @Cacheable(value = "patterns", key = "#userId + '-seasonal'")
    public List<SpendingPattern> getSeasonalPatterns(final UUID userId) {
        return spendingPatternRepository.findSeasonalByUserId(userId);
    }

    @Override
    public List<SpendingPattern> getPatternsByCategory(final UUID userId, final String category) {
        return spendingPatternRepository.findByUserIdAndCategory(userId, category);
    }

    @Override
    public SpendingPattern getPatternById(final UUID patternId, final UUID userId) {
        return spendingPatternRepository.findByIdAndNotDeleted(patternId)
            .filter(pattern -> pattern.getUserId().equals(userId))
            .orElseThrow(() -> new EntityNotFoundException("Pattern not found"));
    }

    private SpendingPattern analyzeCategory(
        final UUID userId,
        final String category,
        final List<Map<String, Object>> transactions
    ) {
        final List<BigDecimal> amounts = transactions.stream()
            .map(t -> new BigDecimal(t.get("amount").toString()))
            .collect(Collectors.toList());

        final BigDecimal total = amounts.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        final BigDecimal average = total.divide(BigDecimal.valueOf(amounts.size()), 2, RoundingMode.HALF_UP);
        final BigDecimal min = amounts.stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        final BigDecimal max = amounts.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);

        final Instant firstOccurrence = Instant.parse(transactions.get(0).get("transactionDate").toString());
        final Instant lastOccurrence = Instant.parse(transactions.get(transactions.size() - 1).get("transactionDate").toString());

        final boolean isRecurring = detectRecurring(transactions);
        final SpendingPattern.Frequency frequency = determineFrequency(transactions);

        return SpendingPattern.builder()
            .userId(userId)
            .patternType(SpendingPattern.PatternType.RECURRING)
            .category(category)
            .frequency(frequency)
            .averageAmount(average)
            .minAmount(min)
            .maxAmount(max)
            .totalAmount(total)
            .transactionCount(transactions.size())
            .firstOccurrence(firstOccurrence)
            .lastOccurrence(lastOccurrence)
            .confidenceScore(new BigDecimal("75.00"))
            .isRecurring(isRecurring)
            .isSeasonal(false)
            .trend(SpendingPattern.Trend.STABLE)
            .metadata(new HashMap<>())
            .build();
    }

    private boolean detectRecurring(final List<Map<String, Object>> transactions) {
        return transactions.size() >= 3;
    }

    private SpendingPattern.Frequency determineFrequency(final List<Map<String, Object>> transactions) {
        if (transactions.size() < 2) {
            return SpendingPattern.Frequency.MONTHLY;
        }

        final long daysBetween = ChronoUnit.DAYS.between(
            Instant.parse(transactions.get(0).get("transactionDate").toString()),
            Instant.parse(transactions.get(transactions.size() - 1).get("transactionDate").toString())
        );

        final long avgDaysBetween = daysBetween / (transactions.size() - 1);

        if (avgDaysBetween <= 7) return SpendingPattern.Frequency.WEEKLY;
        if (avgDaysBetween <= 14) return SpendingPattern.Frequency.BIWEEKLY;
        if (avgDaysBetween <= 31) return SpendingPattern.Frequency.MONTHLY;
        if (avgDaysBetween <= 92) return SpendingPattern.Frequency.QUARTERLY;
        return SpendingPattern.Frequency.YEARLY;
    }

    private void publishPatternIdentifiedEvent(final SpendingPattern pattern) {
        final PatternIdentifiedEvent event = PatternIdentifiedEvent.from(
            pattern.getId(),
            pattern.getUserId(),
            pattern.getPatternType().name(),
            pattern.getCategory(),
            pattern.getMerchantName(),
            pattern.getFrequency().name(),
            pattern.getAverageAmount(),
            pattern.getTransactionCount(),
            pattern.getFirstOccurrence(),
            pattern.getLastOccurrence(),
            pattern.getNextPredictedDate(),
            pattern.getConfidenceScore(),
            pattern.getIsRecurring(),
            pattern.getIsSeasonal(),
            pattern.getTrend() != null ? pattern.getTrend().name() : null,
            pattern.getMetadata()
        );
        kafkaTemplate.send("pattern-identified", event);
    }
}
