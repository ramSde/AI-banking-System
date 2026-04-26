package com.banking.orchestration.dto;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public record ModelConfig(
        UUID id,
        String name,
        String provider,
        String modelType,
        BigDecimal inputPricePer1k,
        BigDecimal outputPricePer1k,
        Integer maxTokens,
        Integer contextWindow,
        Boolean enabled,
        Integer priority,
        Map<String, Object> capabilities,
        Map<String, Object> configuration
) {
}
