package com.banking.insight.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsightGeneratedEvent {

    private UUID insightId;
    private UUID userId;
    private String insightType;
    private String title;
    private String description;
    private String category;
    private String priority;
    private BigDecimal confidenceScore;
    private String aiModel;
    private Integer aiPromptTokens;
    private Integer aiCompletionTokens;
    private BigDecimal aiCost;
    private Map<String, Object> metadata;
    private Instant generatedAt;
    private Instant timestamp;

    public static InsightGeneratedEvent from(
        final UUID insightId,
        final UUID userId,
        final String insightType,
        final String title,
        final String description,
        final String category,
        final String priority,
        final BigDecimal confidenceScore,
        final String aiModel,
        final Integer aiPromptTokens,
        final Integer aiCompletionTokens,
        final BigDecimal aiCost,
        final Map<String, Object> metadata
    ) {
        return InsightGeneratedEvent.builder()
            .insightId(insightId)
            .userId(userId)
            .insightType(insightType)
            .title(title)
            .description(description)
            .category(category)
            .priority(priority)
            .confidenceScore(confidenceScore)
            .aiModel(aiModel)
            .aiPromptTokens(aiPromptTokens)
            .aiCompletionTokens(aiCompletionTokens)
            .aiCost(aiCost)
            .metadata(metadata)
            .generatedAt(Instant.now())
            .timestamp(Instant.now())
            .build();
    }
}
