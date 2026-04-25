package com.banking.transaction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Transaction Reversal Request DTO
 * 
 * Request payload for reversing a completed transaction.
 * Admin-only operation.
 */
public record TransactionReversalRequest(
        @NotBlank(message = "Reason is required")
        @Size(min = 10, max = 500, message = "Reason must be between 10 and 500 characters")
        String reason
) {}
