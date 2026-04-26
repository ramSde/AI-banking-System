package com.banking.insight.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecommendationResponse {

    private UUID id;
    private UUID userId;
    private UUID insightId;
    private String recommendationType;
    private String title;
    private String description;
    private List<String> actionItems;
    private BigDecimal potentialSavings;
    private String priority;
    private String category;
    private BigDecimal confidenceScore;
    private String status;
    private Boolean isAccepted;
    private Boolean isDismissed;
    private Instant acceptedAt;
    private Instant dismissedAt;
    private Instant expiresAt;
    private Map<String, Object> metadata;
    private Instant createdAt;
    private Instant updatedAt;
}
