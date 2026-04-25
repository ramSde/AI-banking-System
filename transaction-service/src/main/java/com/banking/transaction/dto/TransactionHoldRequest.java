package com.banking.transaction.dto;

import com.banking.transaction.domain.HoldType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Transaction Hold Request DTO
 * 
 * Request payload for creating a transaction hold (authorization/reservation).
 */
public record TransactionHoldRequest(
        @NotNull(message = "Account ID is required")
        UUID accountId,

        @NotNull(message = "Hold type is required")
        HoldType holdType,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        @DecimalMax(value = "1000000.00", message = "Amount cannot exceed 1,000,000")
        @Digits(integer = 17, fraction = 2, message = "Amount must have at most 2 decimal places")
        BigDecimal amount,

        @NotBlank(message = "Currency is required")
        @Size(min = 3, max = 3, message = "Currency must be 3 characters")
        String currency,

        @Size(max = 500, message = "Description cannot exceed 500 characters")
        String description,

        @Min(value = 1, message = "Expiry hours must be at least 1")
        @Max(value = 168, message = "Expiry hours cannot exceed 168 (7 days)")
        Integer expiryHours
) {
    public TransactionHoldRequest {
        if (currency == null) {
            currency = "USD";
        }
        if (expiryHours == null) {
            expiryHours = 72;
        }
    }
}
