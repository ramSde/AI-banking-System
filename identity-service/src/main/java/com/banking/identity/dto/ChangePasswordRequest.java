package com.banking.identity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Change Password Request DTO
 * 
 * Request payload for changing user password.
 */
@Schema(description = "Change password request")
public record ChangePasswordRequest(

    @Schema(description = "Current password", example = "OldPass123!", required = true)
    @NotBlank(message = "Current password is required")
    String currentPassword,

    @Schema(description = "New password (min 8 chars, must contain uppercase, lowercase, digit, special char)", example = "NewPass456!", required = true)
    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 100, message = "New password must be between 8 and 100 characters")
    String newPassword
) {
}
