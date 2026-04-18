package com.banking.otp.dto;

import com.banking.otp.domain.MfaMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

/**
 * Request to verify OTP code
 */
@Schema(description = "Request to verify OTP code")
public record VerifyOtpRequest(
        @NotNull(message = "User ID is required")
        @Schema(description = "User ID", example = "550e8400-e29b-41d4-a716-446655440000", required = true)
        UUID userId,

        @NotNull(message = "MFA method is required")
        @Schema(description = "MFA method (SMS or EMAIL)", example = "SMS", required = true)
        MfaMethod method,

        @NotBlank(message = "OTP code is required")
        @Pattern(regexp = "^\\d{6}$", message = "OTP code must be 6 digits")
        @Schema(description = "6-digit OTP code", example = "123456", required = true)
        String code
) {}
