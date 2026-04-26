package com.banking.insight.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AnomalyResponse {

    private UUID id;
    private UUID userId;
    private UUID insightId;
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
    private Boolean isFalsePositive;
    private Boolean isAcknowledged;
    private Instant acknowledgedAt;
    private String resolutionNotes;
    private Map<String, Object> metadata;
    private Instant detectedAt;
    private Instant createdAt;
    private Instant updatedAt;
}
