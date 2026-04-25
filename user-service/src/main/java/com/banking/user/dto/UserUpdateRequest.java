package com.banking.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating user profile.
 * Only non-null fields will be updated.
 */
@Schema(description = "User update request")
public record UserUpdateRequest(
        
        @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone number must be in E.164 format")
        @Schema(description = "Phone number in E.164 format", example = "+1234567890")
        String phoneNumber,

        @Size(max = 500, message = "Address must not exceed 500 characters")
        @Schema(description = "Full address", example = "123 Main St, Apt 4B")
        String address,

        @Size(max = 100, message = "City must not exceed 100 characters")
        @Schema(description = "City", example = "New York")
        String city,

        @Size(max = 100, message = "State must not exceed 100 characters")
        @Schema(description = "State/Province", example = "NY")
        String state,

        @Size(min = 2, max = 3, message = "Country code must be 2-3 characters")
        @Pattern(regexp = "^[A-Z]{2,3}$", message = "Country code must be uppercase ISO 3166-1")
        @Schema(description = "Country code (ISO 3166-1)", example = "US")
        String country,

        @Size(max = 20, message = "Postal code must not exceed 20 characters")
        @Schema(description = "Postal/ZIP code", example = "10001")
        String postalCode
) {
}
