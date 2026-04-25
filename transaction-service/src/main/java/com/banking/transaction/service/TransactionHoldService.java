package com.banking.transaction.service;

import com.banking.transaction.dto.TransactionHoldRequest;
import com.banking.transaction.dto.TransactionHoldResponse;

import java.util.UUID;

/**
 * Transaction Hold Service Interface
 * 
 * Manages authorization holds and reservations on account balances.
 */
public interface TransactionHoldService {

    /**
     * Create a new transaction hold
     */
    TransactionHoldResponse createHold(TransactionHoldRequest request, UUID userId);

    /**
     * Capture a hold (convert to transaction)
     */
    UUID captureHold(UUID holdId, UUID userId);

    /**
     * Release a hold (restore available balance)
     */
    void releaseHold(UUID holdId, UUID userId);

    /**
     * Get hold by ID
     */
    TransactionHoldResponse getHoldById(UUID holdId);
}
