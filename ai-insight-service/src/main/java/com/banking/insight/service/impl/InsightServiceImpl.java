package com.banking.insight.service.impl;

import com.banking.insight.domain.Insight;
import com.banking.insight.dto.InsightRequest;
import com.banking.insight.dto.InsightSummaryResponse;
import com.banking.insight.event.InsightGeneratedEvent;
import com.banking.insight.exception.InsufficientDataException;
import com.banking.insight.repository.AnomalyRepository;
import com.banking.insight.repository.InsightRepository;
import com.banking.insight.repository.RecommendationRepository;
import com.banking.insight.repository.SpendingPatternRepository;
import com.banking.insight.service.*;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class InsightServiceImpl implements InsightService {

    private static final Logger logger = LoggerFactory.getLogger(InsightServiceImpl.class);

    private final InsightRepository insightRepository;
    private final AnomalyRepository anomalyRepository;
    private final RecommendationRepository recommendationRepository;
    private final SpendingPatternRepository spendingPatternRepository;
    private final DataAggregationService dataAggregationService;
    private final PatternAnalysisService patternAnalysisService;
    private final AnomalyDetectionService anomalyDetectionService;
    private final RecommendationService recommendationService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public InsightServiceImpl(
        final InsightRepository insightRepository,
        final AnomalyRepository anomalyRepository,
        final RecommendationRepository recommendationRepository,
        final SpendingPatternRepository spendingPatternRepository,
        final DataAggregationService dataAggregationService,
        final PatternAnalysisService patternAnalysisService,
        final AnomalyDetectionService anomalyDetectionService,
        final RecommendationService recommendationService,
        final KafkaTemplate<String, Object> kafkaTemplate
    ) {
        this.insightRepository = insightRepository;
        this.anomalyRepository = anomalyRepository;
        this.recommendationRepository = recommendationRepository;
        this.spendingPatternRepository = spendingPatternRepository;
        this.dataAggregationService = dataAggregationService;
        this.patternAnalysisService = patternAnalysisService;
        this.anomalyDetectionService = anomalyDetectionService;
        this.recommendationService = recommendationService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    @CacheEvict(value = "insights", key = "#request.userId")
    public Insight generateInsights(final InsightRequest request) {
        logger.info("Generating insights for user: {}", request.getUserId());

        final Instant endDate = request.getEndDate() != null ? request.getEndDate() : Instant.now();
        final Instant startDate = request.getStartDate() != null ? request.getStartDate() : endDate.minus(90, ChronoUnit.DAYS);

        final Integer transactionCount = dataAggregationService.getTransactionCount(request.getUserId(), startDate, endDate);
        if (transactionCount < request.getMinTransactions()) {
            throw new InsufficientDataException(
                String.format("Insufficient transactions. Required: %d, Found: %d", request.getMinTransactions(), transactionCount)
            );
        }

        patternAnalysisService.analyzeSpendingPatterns(request.getUserId());
        
        if (Boolean.TRUE.equals(request.getIncludeAnomalies())) {
            anomalyDetectionService.detectAnomalies(request.getUserId());
        }
        
        if (Boolean.TRUE.equals(request.getIncludeRecommendations())) {
            recommendationService.generateRecommendations(request.getUserId());
        }

        final Insight insight = Insight.builder()
            .userId(request.getUserId())
            .insightType(Insight.InsightType.SPENDING_PATTERN)
            .title("Financial Insights Generated")
            .description("Comprehensive financial analysis completed")
            .priority(Insight.Priority.MEDIUM)
            .confidenceScore(new BigDecimal("85.00"))
            .validFrom(Instant.now())
            .validUntil(Instant.now().plus(30, ChronoUnit.DAYS))
            .metadata(new HashMap<>())
            .build();

        final Insight savedInsight = insightRepository.save(insight);

        publishInsightGeneratedEvent(savedInsight);

        logger.info("Insights generated successfully for user: {}", request.getUserId());
        return savedInsight;
    }

    @Override
    @Cacheable(value = "insights", key = "#insightId")
    public Insight getInsightById(final UUID insightId, final UUID userId) {
        return insightRepository.findByIdAndNotDeleted(insightId)
            .filter(insight -> insight.getUserId().equals(userId))
            .orElseThrow(() -> new EntityNotFoundException("Insight not found"));
    }

    @Override
    public Page<Insight> getUserInsights(final UUID userId, final Pageable pageable) {
        return insightRepository.findByUserIdAndNotDeleted(userId, pageable);
    }

    @Override
    public List<Insight> getUnreadInsights(final UUID userId) {
        return insightRepository.findUnreadByUserId(userId);
    }

    @Override
    public List<Insight> getActiveInsights(final UUID userId) {
        return insightRepository.findActiveByUserId(userId, Instant.now());
    }

    @Override
    public InsightSummaryResponse getInsightSummary(final UUID userId) {
        final long totalInsights = insightRepository.findByUserIdAndNotDeleted(userId, Pageable.unpaged()).getTotalElements();
        final long unreadInsights = insightRepository.countUnreadByUserId(userId);
        final long activeInsights = insightRepository.findActiveByUserId(userId, Instant.now()).size();
        
        final long totalAnomalies = anomalyRepository.findByUserIdAndNotDeleted(userId, Pageable.unpaged()).getTotalElements();
        final long unacknowledgedAnomalies = anomalyRepository.countUnacknowledgedByUserId(userId);
        final long criticalAnomalies = anomalyRepository.findCriticalUnacknowledgedByUserId(userId).size();
        
        final long totalRecommendations = recommendationRepository.findByUserIdAndNotDeleted(userId, Pageable.unpaged()).getTotalElements();
        final long pendingRecommendations = recommendationRepository.countPendingByUserId(userId);
        final BigDecimal totalPotentialSavings = recommendationRepository.calculateTotalPotentialSavings(userId).orElse(BigDecimal.ZERO);
        
        final long totalPatterns = spendingPatternRepository.findByUserIdAndNotDeleted(userId, Pageable.unpaged()).getTotalElements();
        final long recurringPatterns = spendingPatternRepository.findRecurringByUserId(userId).size();

        return InsightSummaryResponse.builder()
            .userId(userId)
            .totalInsights((int) totalInsights)
            .unreadInsights((int) unreadInsights)
            .activeInsights((int) activeInsights)
            .totalAnomalies((int) totalAnomalies)
            .unacknowledgedAnomalies((int) unacknowledgedAnomalies)
            .criticalAnomalies((int) criticalAnomalies)
            .totalRecommendations((int) totalRecommendations)
            .pendingRecommendations((int) pendingRecommendations)
            .totalPotentialSavings(totalPotentialSavings)
            .totalSpendingPatterns((int) totalPatterns)
            .recurringPatterns((int) recurringPatterns)
            .build();
    }

    @Override
    @CacheEvict(value = "insights", key = "#insightId")
    public Insight markAsRead(final UUID insightId, final UUID userId) {
        final Insight insight = getInsightById(insightId, userId);
        insight.markAsRead();
        return insightRepository.save(insight);
    }

    @Override
    @CacheEvict(value = "insights", key = "#insightId")
    public Insight dismissInsight(final UUID insightId, final UUID userId) {
        final Insight insight = getInsightById(insightId, userId);
        insight.dismiss();
        return insightRepository.save(insight);
    }

    @Override
    @CacheEvict(value = "insights", key = "#insightId")
    public void deleteInsight(final UUID insightId, final UUID userId) {
        final Insight insight = getInsightById(insightId, userId);
        insight.setDeletedAt(Instant.now());
        insightRepository.save(insight);
    }

    @Override
    public Page<Insight> getAllInsights(final Pageable pageable) {
        return insightRepository.findAllNotDeleted(pageable);
    }

    private void publishInsightGeneratedEvent(final Insight insight) {
        final InsightGeneratedEvent event = InsightGeneratedEvent.from(
            insight.getId(),
            insight.getUserId(),
            insight.getInsightType().name(),
            insight.getTitle(),
            insight.getDescription(),
            insight.getCategory(),
            insight.getPriority().name(),
            insight.getConfidenceScore(),
            insight.getAiModel(),
            insight.getAiPromptTokens(),
            insight.getAiCompletionTokens(),
            insight.getAiCost(),
            insight.getMetadata()
        );
        kafkaTemplate.send("insight-generated", event);
    }
}
