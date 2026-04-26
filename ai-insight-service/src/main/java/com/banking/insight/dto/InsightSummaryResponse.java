package com.banking.insight.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InsightSummaryResponse {

    private UUID userId;
    private Integer totalInsights;
    private Integer unreadInsights;
    private Integer activeInsights;
    private Integer totalAnomalies;
    private Integer unacknowledgedAnomalies;
    private Integer criticalAnomalies;
    private Integer totalRecommendations;
    private Integer pendingRecommendations;
    private BigDecimal totalPotentialSavings;
    private Integer totalSpendingPatterns;
    private Integer recurringPatterns;
    private List<InsightResponse> recentInsights;
    private List<AnomalyResponse> recentAnomalies;
    private List<RecommendationResponse> topRecommendations;
}
