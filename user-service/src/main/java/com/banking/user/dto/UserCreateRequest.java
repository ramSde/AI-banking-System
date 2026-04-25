package com.banking.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

/**
 * Request DTO for creating a new user profile.
 * All PII fields will be encrypted before storage.
 */
@Schema(description = "User creation request")
public record UserCreateRequest(
        
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 255, message = "Email must not exceed 255 characters")
        @Schema(description = "User email address", example = "john.doe@example.com")
        String email,

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone number must be in E.164 format")
        @Schema(description = "Phone number in E.164 format", example = "+1234567890")
        String phoneNumber,

        @NotBlank(message = "First name is required")
        @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
        @Pattern(regexp = "^[a-zA-Z\\s'-]{2,100}$", message = "First name contains invalid characters")
        @Schema(description = "User first name", example = "John")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters")
        @Pattern(regexp = "^[a-zA-Z\\s'-]{2,100}$", message = "Last name contains invalid characters")
        @Schema(description = "User last name", example = "Doe")
        String lastName,

        @NotNull(message = "Date of birth is required")
        @Past(message = "Date of birth must be in the past")
        @Schema(description = "Date of birth (must be 18+ years old)", example = "1990-01-15")
        LocalDate dateOfBirth,

        @Size(max = 500, message = "Address must not exceed 500 characters")
        @Schema(description = "Full address", example = "123 Main St, Apt 4B")
        String address,

        @Size(max = 100, message = "City must not exceed 100 characters")
        @Schema(description = "City", example = "New York")
        String city,

        @Size(max = 100, message = "State must not exceed 100 characters")
        @Schema(description = "State/Province", example = "NY")
        String state,

        @NotBlank(message = "Country is required")
        @Size(min = 2, max = 3, message = "Country code must be 2-3 characters")
        @Pattern(regexp = "^[A-Z]{2,3}$", message = "Country code must be uppercase ISO 3166-1")
        @Schema(description = "Country code (ISO 3166-1)", example = "US")
        String country,

        @Size(max = 20, message = "Postal code must not exceed 20 characters")
        @Schema(description = "Postal/ZIP code", example = "10001")
        String postalCode
) {
}
