package com.banking.transaction.dto;

import com.banking.transaction.domain.HoldType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Transaction Hold Response DTO
 * 
 * Response payload for transaction hold details.
 */
public record TransactionHoldResponse(
        UUID id,
        UUID accountId,
        HoldType holdType,
        BigDecimal amount,
        String currency,
        String description,
        boolean captured,
        boolean released,
        Instant expiresAt,
        UUID capturedTransactionId,
        Instant capturedAt,
        Instant releasedAt,
        UUID createdBy,
        Instant createdAt,
        Instant updatedAt,
        Long version
) {}
