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
public class InsightResponse {

    private UUID id;
    private UUID userId;
    private String insightType;
    private String title;
    private String description;
    private String category;
    private String priority;
    private BigDecimal confidenceScore;
    private Map<String, Object> metadata;
    private String aiModel;
    private Integer aiPromptTokens;
    private Integer aiCompletionTokens;
    private BigDecimal aiCost;
    private Instant validFrom;
    private Instant validUntil;
    private Boolean isRead;
    private Boolean isDismissed;
    private Instant readAt;
    private Instant dismissedAt;
    private Instant createdAt;
    private Instant updatedAt;
}
