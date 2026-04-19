package com.banking.device.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating device trust score.
 * Used for manual trust adjustments and automated trust updates.
 */
@Schema(description = "Device trust score update request")
public record DeviceTrustUpdateRequest(
        
        @Schema(description = "New trust score (0-100)", example = "85")
        @NotNull(message = "Trust score is required")
        @Min(value = 0, message = "Trust score must be at least 0")
        @Max(value = 100, message = "Trust score must not exceed 100")
        Integer trustScore,
        
        @Schema(description = "Reason for trust change", example = "Successful authentication")
        @Size(max = 255, message = "Reason must not exceed 255 characters")
        String reason,
        
        @Schema(description = "Additional metadata as JSON", example = "{\"source\": \"manual\", \"admin_id\": \"admin123\"}")
        String metadata
) {
}