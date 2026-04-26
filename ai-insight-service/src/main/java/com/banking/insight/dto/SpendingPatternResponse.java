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
public class SpendingPatternResponse {

    private UUID id;
    private UUID userId;
    private String patternType;
    private String category;
    private String merchantName;
    private String frequency;
    private BigDecimal averageAmount;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private BigDecimal totalAmount;
    private Integer transactionCount;
    private Instant firstOccurrence;
    private Instant lastOccurrence;
    private Instant nextPredictedDate;
    private BigDecimal confidenceScore;
    private Boolean isRecurring;
    private Boolean isSeasonal;
    private String season;
    private String trend;
    private Map<String, Object> metadata;
    private Instant createdAt;
    private Instant updatedAt;
}
