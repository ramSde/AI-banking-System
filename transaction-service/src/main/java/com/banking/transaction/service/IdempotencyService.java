package com.banking.transaction.service;

import com.banking.transaction.domain.IdempotencyKey;

import java.util.Optional;
import java.util.UUID;

/**
 * Idempotency Service Interface
 * 
 * Manages idempotency keys for duplicate request prevention.
 */
public interface IdempotencyService {

    /**
     * Check if idempotency key exists and is valid
     */
    Optional<IdempotencyKey> findByKey(String key);

    /**
     * Store idempotency key with response
     */
    void storeKey(String key, UUID transactionId, String requestHash, String responseBody, int responseStatus);

    /**
     * Validate request hash matches stored hash
     */
    boolean validateRequestHash(String key, String requestHash);

    /**
     * Clean up expired idempotency keys
     */
    int cleanupExpiredKeys();
}
