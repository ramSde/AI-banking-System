package com.banking.fraud.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * Fraud Check Request DTO
 * 
 * @param transactionId Transaction ID to check
 * @param userId User ID
 * @param amount Transaction amount
 * @param transactionType Transaction type
 * @param metadata Additional metadata for fraud check
 */
public record FraudCheckRequest(
        @NotNull(message = "Transaction ID is required")
        UUID transactionId,

        @NotNull(message = "User ID is required")
        UUID userId,

        @NotNull(message = "Amount is required")
        BigDecimal amount,

        @NotNull(message = "Transaction type is required")
        String transactionType,

        Map<String, Object> metadata
) {
}
