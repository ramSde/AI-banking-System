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
public class PatternIdentifiedEvent {

    private UUID patternId;
    private UUID userId;
    private String patternType;
    private String category;
    private String merchantName;
    private String frequency;
    private BigDecimal averageAmount;
    private Integer transactionCount;
    private Instant firstOccurrence;
    private Instant lastOccurrence;
    private Instant nextPredictedDate;
    private BigDecimal confidenceScore;
    private Boolean isRecurring;
    private Boolean isSeasonal;
    private String trend;
    private Map<String, Object> metadata;
    private Instant identifiedAt;
    private Instant timestamp;

    public static PatternIdentifiedEvent from(
        final UUID patternId,
        final UUID userId,
        final String patternType,
        final String category,
        final String merchantName,
        final String frequency,
        final BigDecimal averageAmount,
        final Integer transactionCount,
        final Instant firstOccurrence,
        final Instant lastOccurrence,
        final Instant nextPredictedDate,
        final BigDecimal confidenceScore,
        final Boolean isRecurring,
        final Boolean isSeasonal,
        final String trend,
        final Map<String, Object> metadata
    ) {
        return PatternIdentifiedEvent.builder()
            .patternId(patternId)
            .userId(userId)
            .patternType(patternType)
            .category(category)
            .merchantName(merchantName)
            .frequency(frequency)
            .averageAmount(averageAmount)
            .transactionCount(transactionCount)
            .firstOccurrence(firstOccurrence)
            .lastOccurrence(lastOccurrence)
            .nextPredictedDate(nextPredictedDate)
            .confidenceScore(confidenceScore)
            .isRecurring(isRecurring)
            .isSeasonal(isSeasonal)
            .trend(trend)
            .metadata(metadata)
            .identifiedAt(Instant.now())
            .timestamp(Instant.now())
            .build();
    }
}
