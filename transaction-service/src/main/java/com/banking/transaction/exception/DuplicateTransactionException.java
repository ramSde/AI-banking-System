package com.banking.transaction.exception;

/**
 * Duplicate Transaction Exception
 * 
 * Thrown when a duplicate transaction is detected via idempotency key.
 */
public class DuplicateTransactionException extends TransactionException {

    public DuplicateTransactionException(String idempotencyKey) {
        super(String.format("Duplicate transaction detected with idempotency key: %s", idempotencyKey));
    }
}
