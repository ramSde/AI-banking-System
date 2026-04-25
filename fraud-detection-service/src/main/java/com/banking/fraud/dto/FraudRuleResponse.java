package com.banking.fraud.dto;

import com.banking.fraud.domain.RuleType;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Fraud Rule Response DTO
 * 
 * @param id Rule ID
 * @param ruleName Rule name
 * @param ruleType Rule type
 * @param description Rule description
 * @param ruleConfig Rule configuration
 * @param weight Rule weight
 * @param enabled Whether rule is enabled
 * @param createdBy Creator user ID
 * @param createdAt Creation timestamp
 * @param updatedAt Update timestamp
 */
public record FraudRuleResponse(
        UUID id,
        String ruleName,
        RuleType ruleType,
        String description,
        Map<String, Object> ruleConfig,
        Integer weight,
        Boolean enabled,
        UUID createdBy,
        Instant createdAt,
        Instant updatedAt
) {
}
