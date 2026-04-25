package com.banking.transaction.dto;

import com.banking.transaction.domain.EntryType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Ledger Entry Response DTO
 * 
 * Response payload for ledger entry details.
 * Used for transaction audit trail and reconciliation.
 */
public record LedgerEntryResponse(
        UUID id,
        UUID transactionId,
        UUID accountId,
        EntryType entryType,
        BigDecimal amount,
        String currency,
        BigDecimal balanceBefore,
        BigDecimal balanceAfter,
        String description,
        Instant createdAt
) {}
