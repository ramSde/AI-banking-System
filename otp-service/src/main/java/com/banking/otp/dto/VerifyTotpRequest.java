package com.banking.otp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

/**
 * Request to verify TOTP code
 */
@Schema(description = "Request to verify TOTP code")
public record VerifyTotpRequest(
        @NotNull(message = "User ID is required")
        @Schema(description = "User ID", example = "550e8400-e29b-41d4-a716-446655440000", required = true)
        UUID userId,

        @NotBlank(message = "TOTP code is required")
        @Pattern(regexp = "^\\d{6}$", message = "TOTP code must be 6 digits")
        @Schema(description = "6-digit TOTP code", example = "123456", required = true)
        String code
) {}
