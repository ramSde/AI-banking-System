package com.banking.insight.service;

import com.banking.insight.domain.Recommendation;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface RecommendationService {

    List<Recommendation> generateRecommendations(UUID userId);

    List<Recommendation> getActionableRecommendations(UUID userId);

    List<Recommendation> getPendingRecommendations(UUID userId);

    Recommendation getRecommendationById(UUID recommendationId, UUID userId);

    Recommendation acceptRecommendation(UUID recommendationId, UUID userId);

    Recommendation dismissRecommendation(UUID recommendationId, UUID userId);

    Recommendation completeRecommendation(UUID recommendationId, UUID userId);

    BigDecimal calculateTotalPotentialSavings(UUID userId);
}
