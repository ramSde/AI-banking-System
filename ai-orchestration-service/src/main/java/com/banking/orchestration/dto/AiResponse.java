package com.banking.orchestration.dto;

import java.time.Instant;
import java.util.Map;

public record AiResponse(
        String response,
        String modelUsed,
        String provider,
        Integer inputTokens,
        Integer outputTokens,
        Integer totalTokens,
        Long latencyMs,
        java.math.BigDecimal costUsd,
        String sessionId,
        String traceId,
        Instant timestamp,
        Map<String, Object> metadata
) {
}
