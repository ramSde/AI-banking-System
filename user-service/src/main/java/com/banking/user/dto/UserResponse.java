package com.banking.user.dto;

import com.banking.user.domain.KycStatus;
import com.banking.user.domain.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Response DTO for user profile.
 * Contains masked PII fields for security.
 */
@Schema(description = "User profile response")
public record UserResponse(
        
        @Schema(description = "User ID", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID id,

        @Schema(description = "Email address", example = "john.doe@example.com")
        String email,

        @Schema(description = "Masked phone number", example = "+******7890")
        String phoneNumber,

        @Schema(description = "Masked first name", example = "J**n")
        String firstName,

        @Schema(description = "Masked last name", example = "D*e")
        String lastName,

        @Schema(description = "Masked date of birth", example = "****-**-15")
        String dateOfBirth,

        @Schema(description = "Masked address", example = "***")
        String address,

        @Schema(description = "City", example = "New York")
        String city,

        @Schema(description = "State", example = "NY")
        String state,

        @Schema(description = "Country", example = "US")
        String country,

        @Schema(description = "Postal code", example = "10001")
        String postalCode,

        @Schema(description = "User status", example = "ACTIVE")
        UserStatus userStatus,

        @Schema(description = "KYC status", example = "VERIFIED")
        KycStatus kycStatus,

        @Schema(description = "KYC verified timestamp")
        Instant kycVerifiedAt,

        @Schema(description = "Last login timestamp")
        Instant lastLoginAt,

        @Schema(description = "Account creation timestamp")
        Instant createdAt,

        @Schema(description = "Last update timestamp")
        Instant updatedAt
) {
}
