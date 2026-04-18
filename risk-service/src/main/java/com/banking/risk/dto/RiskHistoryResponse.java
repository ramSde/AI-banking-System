package com.banking.risk.dto;

import com.banking.risk.domain.RiskAction;
import com.banking.risk.domain.RiskLevel;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for risk history records.
 * Provides historical risk assessment data for analytics.
 */
@Schema(description = "Historical risk assessment record")
public record RiskHistoryResponse(

        @Schema(description = "History record ID")
        UUID id,

        @Schema(description = "User ID")
        UUID userId,

        @Schema(description = "Assessment ID")
        UUID assessmentId,

        @Schema(description = "Risk score at the time of assessment")
        Integer riskScore,

        @Schema(description = "Risk level at the time of assessment")
        RiskLevel riskLevel,

        @Schema(description = "Action that was taken")
        RiskAction actionTaken,

        @Schema(description = "Timestamp of the assessment")
        Instant createdAt

) {
}
