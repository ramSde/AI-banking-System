package com.banking.risk.dto;

import com.banking.risk.domain.RiskRuleType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.util.Map;

/**
 * Request DTO for creating or updating risk rules.
 * Defines the rule configuration and its impact on risk scoring.
 */
@Schema(description = "Risk rule configuration request")
public record RiskRuleRequest(

        @Schema(description = "Unique rule name", example = "New Device Detection", required = true)
        @NotBlank(message = "Rule name is required")
        @Size(max = 100, message = "Rule name must not exceed 100 characters")
        String name,

        @Schema(description = "Rule description", example = "Detects when a user logs in from a new device")
        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description,

        @Schema(description = "Rule type: DEVICE, LOCATION, VELOCITY, TIME, FAILED_ATTEMPTS, CUSTOM", 
                example = "DEVICE", required = true)
        @NotNull(message = "Rule type is required")
        RiskRuleType ruleType,

        @Schema(description = "JSON condition for rule evaluation", required = true)
        @NotNull(message = "Condition is required")
        Map<String, Object> condition,

        @Schema(description = "Impact on risk score when rule matches (0-100)", example = "25", required = true)
        @NotNull(message = "Risk score impact is required")
        @Min(value = 0, message = "Risk score impact must be at least 0")
        @Max(value = 100, message = "Risk score impact must not exceed 100")
        Integer riskScoreImpact,

        @Schema(description = "Whether the rule is enabled", example = "true", required = true)
        @NotNull(message = "Enabled flag is required")
        Boolean enabled,

        @Schema(description = "Rule evaluation priority (higher = evaluated first)", example = "100", required = true)
        @NotNull(message = "Priority is required")
        @Min(value = 0, message = "Priority must be at least 0")
        Integer priority

) {
}
