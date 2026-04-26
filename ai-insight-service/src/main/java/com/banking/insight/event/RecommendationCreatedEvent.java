package com.banking.insight.event;

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
public class RecommendationCreatedEvent {

    private UUID recommendationId;
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
    private Map<String, Object> metadata;
    private Instant createdAt;
    private Instant timestamp;

    public static RecommendationCreatedEvent from(
        final UUID recommendationId,
        final UUID userId,
        final UUID insightId,
        final String recommendationType,
        final String title,
        final String description,
        final List<String> actionItems,
        final BigDecimal potentialSavings,
        final String priority,
        final String category,
        final BigDecimal confidenceScore,
        final Map<String, Object> metadata
    ) {
        return RecommendationCreatedEvent.builder()
            .recommendationId(recommendationId)
            .userId(userId)
            .insightId(insightId)
            .recommendationType(recommendationType)
            .title(title)
            .description(description)
            .actionItems(actionItems)
            .potentialSavings(potentialSavings)
            .priority(priority)
            .category(category)
            .confidenceScore(confidenceScore)
            .metadata(metadata)
            .createdAt(Instant.now())
            .timestamp(Instant.now())
            .build();
    }
}
