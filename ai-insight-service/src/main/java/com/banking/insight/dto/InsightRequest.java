package com.banking.insight.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InsightRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    private List<String> insightTypes;

    private Instant startDate;

    private Instant endDate;

    @Positive(message = "Minimum transactions must be positive")
    @Builder.Default
    private Integer minTransactions = 10;

    @Builder.Default
    private Boolean includeAnomalies = true;

    @Builder.Default
    private Boolean includeRecommendations = true;

    @Builder.Default
    private Boolean includeForecasts = true;

    private List<String> categories;

    private String aiModel;
}
