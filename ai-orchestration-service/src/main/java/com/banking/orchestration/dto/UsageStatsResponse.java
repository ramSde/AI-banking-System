package com.banking.orchestration.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public record UsageStatsResponse(
        Integer totalRequests,
        Integer successfulRequests,
        Integer failedRequests,
        Long totalTokens,
        Long inputTokens,
        Long outputTokens,
        BigDecimal totalCost,
        Long averageLatencyMs,
        Map<String, Integer> modelDistribution,
        Map<String, Integer> featureDistribution,
        List<UsageEntry> recentUsage,
        Instant periodStart,
        Instant periodEnd
) {
    public record UsageEntry(
            String modelName,
            String provider,
            String feature,
            Integer totalTokens,
            BigDecimal costUsd,
            Long latencyMs,
            Boolean success,
            Instant timestamp
    ) {
    }
}
