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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ForecastResponse {

    private String period;
    private Instant forecastDate;
    private BigDecimal predictedAmount;
    private BigDecimal confidenceInterval;
    private BigDecimal lowerBound;
    private BigDecimal upperBound;
    private String category;
    private List<DataPoint> historicalData;
    private Map<String, Object> metadata;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataPoint {
        private Instant date;
        private BigDecimal amount;
    }
}
