package com.banking.insight.service.impl;

import com.banking.insight.domain.Recommendation;
import com.banking.insight.domain.SpendingPattern;
import com.banking.insight.event.RecommendationCreatedEvent;
import com.banking.insight.repository.RecommendationRepository;
import com.banking.insight.repository.SpendingPatternRepository;
import com.banking.insight.service.RecommendationService;
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
public class AiRecommendationService implements RecommendationService {

    private static final Logger logger = LoggerFactory.getLogger(AiRecommendationService.class);

    private final RecommendationRepository recommendationRepository;
    private final SpendingPatternRepository spendingPatternRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public AiRecommendationService(
        final RecommendationRepository recommendationRepository,
        final SpendingPatternRepository spendingPatternRepository,
        final KafkaTemplate<String, Object> kafkaTemplate
    ) {
        this.recommendationRepository = recommendationRepository;
        this.spendingPatternRepository = spendingPatternRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public List<Recommendation> generateRecommendations(final UUID userId) {
        logger.info("Generating recommendations for user: {}", userId);

        final List<SpendingPattern> patterns = spendingPatternRepository.findByUserIdAndNotDeleted(userId, org.springframework.data.domain.Pageable.unpaged()).getContent();
        final List<Recommendation> recommendations = new ArrayList<>();

        for (final SpendingPattern pattern : patterns) {
            if (pattern.getAverageAmount().compareTo(new BigDecimal("100")) > 0) {
                final Recommendation recommendation = createSavingRecommendation(userId, pattern);
                recommendations.add(recommendationRepository.save(recommendation));
                publishRecommendationCreatedEvent(recommendation);
            }
        }

        logger.info("Generated {} recommendations for user: {}", recommendations.size(), userId);
        return recommendations;
    }

    @Override
    @Cacheable(value = "recommendations", key = "#userId + '-actionable'")
    public List<Recommendation> getActionableRecommendations(final UUID userId) {
        return recommendationRepository.findActionableByUserId(userId, Instant.now());
    }

    @Override
    public List<Recommendation> getPendingRecommendations(final UUID userId) {
        return recommendationRepository.findByUserIdAndStatus(userId, Recommendation.Status.PENDING);
    }

    @Override
    public Recommendation getRecommendationById(final UUID recommendationId, final UUID userId) {
        return recommendationRepository.findByIdAndNotDeleted(recommendationId)
            .filter(rec -> rec.getUserId().equals(userId))
            .orElseThrow(() -> new EntityNotFoundException("Recommendation not found"));
    }

    @Override
    @CacheEvict(value = "recommendations", key = "#userId + '-actionable'")
    public Recommendation acceptRecommendation(final UUID recommendationId, final UUID userId) {
        final Recommendation recommendation = getRecommendationById(recommendationId, userId);
        recommendation.accept();
        return recommendationRepository.save(recommendation);
    }

    @Override
    @CacheEvict(value = "recommendations", key = "#userId + '-actionable'")
    public Recommendation dismissRecommendation(final UUID recommendationId, final UUID userId) {
        final Recommendation recommendation = getRecommendationById(recommendationId, userId);
        recommendation.dismiss();
        return recommendationRepository.save(recommendation);
    }

    @Override
    @CacheEvict(value = "recommendations", key = "#userId + '-actionable'")
    public Recommendation completeRecommendation(final UUID recommendationId, final UUID userId) {
        final Recommendation recommendation = getRecommendationById(recommendationId, userId);
        recommendation.complete();
        return recommendationRepository.save(recommendation);
    }

    @Override
    public BigDecimal calculateTotalPotentialSavings(final UUID userId) {
        return recommendationRepository.calculateTotalPotentialSavings(userId).orElse(BigDecimal.ZERO);
    }

    private Recommendation createSavingRecommendation(final UUID userId, final SpendingPattern pattern) {
        final BigDecimal potentialSavings = pattern.getAverageAmount().multiply(new BigDecimal("0.10"));

        return Recommendation.builder()
            .userId(userId)
            .recommendationType(Recommendation.RecommendationType.SAVE_MONEY)
            .title(String.format("Reduce %s spending", pattern.getCategory()))
            .description(String.format("You spend an average of %s on %s. Consider reducing by 10%%.", 
                pattern.getAverageAmount(), pattern.getCategory()))
            .actionItems(Arrays.asList(
                "Review recent transactions in this category",
                "Set a monthly budget limit",
                "Look for cheaper alternatives"
            ))
            .potentialSavings(potentialSavings)
            .priority(Recommendation.Priority.MEDIUM)
            .category(pattern.getCategory())
            .confidenceScore(new BigDecimal("80.00"))
            .expiresAt(Instant.now().plus(30, ChronoUnit.DAYS))
            .metadata(new HashMap<>())
            .build();
    }

    private void publishRecommendationCreatedEvent(final Recommendation recommendation) {
        final RecommendationCreatedEvent event = RecommendationCreatedEvent.from(
            recommendation.getId(),
            recommendation.getUserId(),
            recommendation.getInsightId(),
            recommendation.getRecommendationType().name(),
            recommendation.getTitle(),
            recommendation.getDescription(),
            recommendation.getActionItems(),
            recommendation.getPotentialSavings(),
            recommendation.getPriority().name(),
            recommendation.getCategory(),
            recommendation.getConfidenceScore(),
            recommendation.getMetadata()
        );
        kafkaTemplate.send("recommendation-created", event);
    }
}
