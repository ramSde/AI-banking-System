package com.banking.otp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request to enroll in TOTP-based MFA
 */
@Schema(description = "Request to enroll in TOTP-based MFA")
public record EnrollTotpRequest(
        @NotNull(message = "User ID is required")
        @Schema(description = "User ID", example = "550e8400-e29b-41d4-a716-446655440000", required = true)
        UUID userId
) {}
