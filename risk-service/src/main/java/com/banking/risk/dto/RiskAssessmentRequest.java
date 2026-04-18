package com.banking.risk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.Map;
import java.util.UUID;

/**
 * Request DTO for risk assessment.
 * Contains all information needed to evaluate authentication risk.
 */
@Schema(description = "Risk assessment request containing authentication context")
public record RiskAssessmentRequest(

        @Schema(description = "User ID being assessed", example = "550e8400-e29b-41d4-a716-446655440000", required = true)
        @NotNull(message = "User ID is required")
        UUID userId,

        @Schema(description = "Session ID for the authentication attempt", example = "abc-123-def-456", required = true)
        @NotNull(message = "Session ID is required")
        UUID sessionId,

        @Schema(description = "Device fingerprint hash", example = "chrome-windows-192.168.1.1")
        String deviceFingerprint,

        @Schema(description = "IP address of the authentication attempt", example = "192.168.1.1", required = true)
        @NotBlank(message = "IP address is required")
        @Pattern(regexp = "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$|^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$",
                message = "Invalid IP address format")
        String ipAddress,

        @Schema(description = "User agent string", example = "Mozilla/5.0...")
        String userAgent,

        @Schema(description = "Geographic location data")
        GeolocationData geolocation

) {
    /**
     * Geolocation data nested record.
     */
    @Schema(description = "Geographic location information")
    public record GeolocationData(
            @Schema(description = "Country code", example = "US")
            String country,

            @Schema(description = "City name", example = "New York")
            String city,

            @Schema(description = "Latitude", example = "40.7128")
            Double latitude,

            @Schema(description = "Longitude", example = "-74.0060")
            Double longitude
    ) {
        public Map<String, Object> toMap() {
            return Map.of(
                    "country", country != null ? country : "",
                    "city", city != null ? city : "",
                    "latitude", latitude != null ? latitude : 0.0,
                    "longitude", longitude != null ? longitude : 0.0
            );
        }
    }
}
