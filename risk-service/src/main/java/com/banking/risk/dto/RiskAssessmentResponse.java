package com.banking.risk.dto;

import com.banking.risk.domain.RiskAction;
import com.banking.risk.domain.RiskLevel;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Response DTO for risk assessment results.
 * Contains the calculated risk score, level, and recommended action.
 */
@Schema(description = "Risk assessment result with score, level, and recommended action")
public record RiskAssessmentResponse(

        @Schema(description = "Assessment ID", example = "assessment-uuid")
        UUID assessmentId,

        @Schema(description = "Calculated risk score (0-100)", example = "45")
        Integer riskScore,

        @Schema(description = "Risk level: LOW, MEDIUM, HIGH", example = "MEDIUM")
        RiskLevel riskLevel,

        @Schema(description = "Recommended action: ALLOW, REQUIRE_MFA, BLOCK", example = "REQUIRE_MFA")
        RiskAction riskAction,

        @Schema(description = "Breakdown of risk factors and their scores")
        Map<String, Integer> factors,

        @Schema(description = "Whether MFA is required", example = "true")
        Boolean mfaRequired,

        @Schema(description = "Timestamp when assessment was performed")
        Instant assessedAt

) {
}
