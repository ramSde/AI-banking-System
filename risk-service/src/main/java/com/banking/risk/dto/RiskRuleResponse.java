package com.banking.risk.dto;

import com.banking.risk.domain.RiskRuleType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Response DTO for risk rule information.
 * Contains complete risk rule configuration and metadata.
 */
@Schema(description = "Risk rule configuration and metadata")
public record RiskRuleResponse(

        @Schema(description = "Rule ID")
        UUID id,

        @Schema(description = "Rule name")
        String name,

        @Schema(description = "Rule description")
        String description,

        @Schema(description = "Rule type")
        RiskRuleType ruleType,

        @Schema(description = "JSON condition for rule evaluation")
        Map<String, Object> condition,

        @Schema(description = "Impact on risk score when rule matches")
        Integer riskScoreImpact,

        @Schema(description = "Whether the rule is enabled")
        Boolean enabled,

        @Schema(description = "Rule evaluation priority")
        Integer priority,

        @Schema(description = "Creation timestamp")
        Instant createdAt,

        @Schema(description = "Last update timestamp")
        Instant updatedAt

) {
}
