package com.banking.transaction.dto;

import com.banking.transaction.domain.TransactionStatus;
import com.banking.transaction.domain.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Transaction Response DTO
 * 
 * Response payload containing transaction details.
 * Returned for all transaction queries and creation.
 */
public record TransactionResponse(
        UUID id,
        String referenceNumber,
        TransactionType transactionType,
        TransactionStatus transactionStatus,
        UUID sourceAccountId,
        UUID destinationAccountId,
        BigDecimal amount,
        String currency,
        String description,
        Map<String, Object> metadata,
        UUID initiatedBy,
        Instant initiatedAt,
        Instant completedAt,
        Instant failedAt,
        String failureReason,
        Instant reversedAt,
        String reversalReference,
        UUID parentTransactionId,
        Instant createdAt,
        Instant updatedAt,
        Long version
) {}
