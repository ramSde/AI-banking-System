package com.banking.otp.dto;

import com.banking.otp.domain.MfaMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

/**
 * Request to send OTP via SMS or Email
 */
@Schema(description = "Request to send OTP via SMS or Email")
public record SendOtpRequest(
        @NotNull(message = "User ID is required")
        @Schema(description = "User ID", example = "550e8400-e29b-41d4-a716-446655440000", required = true)
        UUID userId,

        @NotNull(message = "MFA method is required")
        @Schema(description = "MFA method (SMS or EMAIL)", example = "SMS", required = true)
        MfaMethod method,

        @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format (E.164)")
        @Schema(description = "Phone number in E.164 format (required for SMS)", example = "+1234567890")
        String phoneNumber,

        @Email(message = "Invalid email format")
        @Schema(description = "Email address (required for EMAIL)", example = "user@example.com")
        String email
) {}
