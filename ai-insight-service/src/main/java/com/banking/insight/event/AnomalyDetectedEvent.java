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
public class AnomalyDetectedEvent {

    private UUID anomalyId;
    private UUID userId;
    private UUID transactionId;
    private String anomalyType;
    private String severity;
    private String description;
    private BigDecimal detectedValue;
    private BigDecimal expectedValue;
    private BigDecimal deviationPercentage;
    private BigDecimal zScore;
    private String category;
    private String merchantName;
    private String detectionMethod;
    private BigDecimal confidenceScore;
    private Map<String, Object> metadata;
    private Instant detectedAt;
    private Instant timestamp;

    public static AnomalyDetectedEvent from(
        final UUID anomalyId,
        final UUID userId,
        final UUID transactionId,
        final String anomalyType,
        final String severity,
        final String description,
        final BigDecimal detectedValue,
        final BigDecimal expectedValue,
        final BigDecimal deviationPercentage,
        final BigDecimal zScore,
        final String category,
        final String merchantName,
        final String detectionMethod,
        final BigDecimal confidenceScore,
        final Map<String, Object> metadata
    ) {
        return AnomalyDetectedEvent.builder()
            .anomalyId(anomalyId)
            .userId(userId)
            .transactionId(transactionId)
            .anomalyType(anomalyType)
            .severity(severity)
            .description(description)
            .detectedValue(detectedValue)
            .expectedValue(expectedValue)
            .deviationPercentage(deviationPercentage)
            .zScore(zScore)
            .category(category)
            .merchantName(merchantName)
            .detectionMethod(detectionMethod)
            .confidenceScore(confidenceScore)
            .metadata(metadata)
            .detectedAt(Instant.now())
            .timestamp(Instant.now())
            .build();
    }
}
