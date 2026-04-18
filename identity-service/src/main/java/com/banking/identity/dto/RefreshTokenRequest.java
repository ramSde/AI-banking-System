package com.banking.identity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Refresh Token Request DTO
 * 
 * Request payload for refreshing access token using refresh token.
 */
@Schema(description = "Refresh token request")
public record RefreshTokenRequest(

    @Schema(description = "Refresh token", example = "rt_abc123def456...", required = true)
    @NotBlank(message = "Refresh token is required")
    String refreshToken,

    @Schema(description = "Device identifier for tracking", example = "device-uuid-12345")
    String deviceId,

    @Schema(description = "IP address of the client", example = "192.168.1.1")
    String ipAddress,

    @Schema(description = "User agent string", example = "Mozilla/5.0...")
    String userAgent
) {
}
