package com.banking.transaction.service;

import com.banking.transaction.domain.LedgerEntry;
import com.banking.transaction.dto.LedgerEntryResponse;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Ledger Service Interface
 * 
 * Manages double-entry ledger operations for all transactions.
 */
public interface LedgerService {

    /**
     * Create ledger entries for a transaction (double-entry bookkeeping)
     */
    void createLedgerEntries(UUID transactionId, UUID sourceAccountId, UUID destinationAccountId,
                             BigDecimal amount, String currency, String description,
                             BigDecimal sourceBalanceBefore, BigDecimal destBalanceBefore);

    /**
     * Get ledger entries for a transaction
     */
    List<LedgerEntryResponse> getLedgerEntriesByTransactionId(UUID transactionId);

    /**
     * Get ledger entries for an account
     */
    List<LedgerEntryResponse> getLedgerEntriesByAccountId(UUID accountId);

    /**
     * Get ledger entries for an account within date range
     */
    List<LedgerEntryResponse> getLedgerEntriesByAccountIdAndDateRange(UUID accountId, Instant startDate, Instant endDate);
}
