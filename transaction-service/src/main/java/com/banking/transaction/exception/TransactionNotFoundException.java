package com.banking.transaction.exception;

import java.util.UUID;

/**
 * Transaction Not Found Exception
 * 
 * Thrown when a transaction cannot be found by ID or reference number.
 */
public class TransactionNotFoundException extends TransactionException {

    public TransactionNotFoundException(UUID id) {
        super(String.format("Transaction not found with ID: %s", id));
    }

    public TransactionNotFoundException(String referenceNumber) {
        super(String.format("Transaction not found with reference: %s", referenceNumber));
    }
}
