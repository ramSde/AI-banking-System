package com.banking.fraud.dto;

import com.banking.fraud.domain.RuleType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

/**
 * Fraud Rule Request DTO
 * 
 * @param ruleName Rule name
 * @param ruleType Rule type
 * @param description Rule description
 * @param ruleConfig Rule configuration JSON
 * @param weight Rule weight (1-100)
 * @param enabled Whether rule is enabled
 */
public record FraudRuleRequest(
        @NotBlank(message = "Rule name is required")
        String ruleName,

        @NotNull(message = "Rule type is required")
        RuleType ruleType,

        String description,

        @NotNull(message = "Rule configuration is required")
        Map<String, Object> ruleConfig,

        @NotNull(message = "Weight is required")
        @Min(value = 1, message = "Weight must be at least 1")
        @Max(value = 100, message = "Weight must not exceed 100")
        Integer weight,

        Boolean enabled
) {
}
