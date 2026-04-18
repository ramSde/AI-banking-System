package com.banking.otp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

/**
 * Request to verify backup code
 */
@Schema(description = "Request to verify backup code")
public record VerifyBackupCodeRequest(
        @NotNull(message = "User ID is required")
        @Schema(description = "User ID", example = "550e8400-e29b-41d4-a716-446655440000", required = true)
        UUID userId,

        @NotBlank(message = "Backup code is required")
        @Pattern(regexp = "^[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}$", message = "Invalid backup code format")
        @Schema(description = "Backup code in format XXXX-XXXX-XXXX", example = "ABCD-1234-EFGH", required = true)
        String code
) {}
