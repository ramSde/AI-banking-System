package com.banking.transaction.dto;

import com.banking.transaction.domain.TransactionType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * Transaction Create Request DTO
 * 
 * Request payload for creating a new transaction.
 * Requires idempotency key in header for duplicate prevention.
 */
public record TransactionCreateRequest(
        @NotNull(message = "Transaction type is required")
        TransactionType transactionType,

        UUID sourceAccountId,

        UUID destinationAccountId,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        @DecimalMax(value = "1000000.00", message = "Amount cannot exceed 1,000,000")
        @Digits(integer = 17, fraction = 2, message = "Amount must have at most 2 decimal places")
        BigDecimal amount,

        @NotBlank(message = "Currency is required")
        @Size(min = 3, max = 3, message = "Currency must be 3 characters (ISO 4217)")
        @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be uppercase ISO 4217 code")
        String currency,

        @Size(max = 500, message = "Description cannot exceed 500 characters")
        String description,

        Map<String, Object> metadata
) {
    public TransactionCreateRequest {
        if (currency == null) {
            currency = "USD";
        }
    }
}
