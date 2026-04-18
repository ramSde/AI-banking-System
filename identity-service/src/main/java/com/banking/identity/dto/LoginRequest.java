package com.banking.identity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Login Request DTO
 * 
 * Request payload for user authentication.
 */
@Schema(description = "User login request")
public record LoginRequest(

    @Schema(description = "User email address", example = "user@example.com", required = true)
    @NotBlank(message = "Email is required")
    String email,

    @Schema(description = "User password", example = "SecurePass123!", required = true)
    @NotBlank(message = "Password is required")
    String password,

    @Schema(description = "Device identifier for tracking", example = "device-uuid-12345")
    String deviceId,

    @Schema(description = "IP address of the client", example = "192.168.1.1")
    String ipAddress,

    @Schema(description = "User agent string", example = "Mozilla/5.0...")
    String userAgent
) {
}
