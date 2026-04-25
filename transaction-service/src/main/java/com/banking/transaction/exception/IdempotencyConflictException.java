package com.banking.transaction.exception;

/**
 * Idempotency Conflict Exception
 * 
 * Thrown when the same idempotency key is used with different request data.
 */
public class IdempotencyConflictException extends TransactionException {

    public IdempotencyConflictException(String idempotencyKey) {
        super(String.format("Idempotency key conflict: same key used with different request data: %s", idempotencyKey));
    }
}
