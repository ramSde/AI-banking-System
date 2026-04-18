package com.banking.identity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Register Request DTO
 * 
 * Request payload for user registration.
 */
@Schema(description = "User registration request")
public record RegisterRequest(

    @Schema(description = "User email address", example = "user@example.com", required = true)
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    String email,

    @Schema(description = "User password (min 8 chars, must contain uppercase, lowercase, digit, special char)", example = "SecurePass123!", required = true)
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    String password,

    @Schema(description = "Phone number in E.164 format", example = "+1234567890")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone number must be in E.164 format")
    String phoneNumber,

    @Schema(description = "Optional username", example = "johndoe")
    @Size(min = 3, max = 100, message = "Username must be between 3 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Username can only contain letters, numbers, underscores, and hyphens")
    String username
) {
}
