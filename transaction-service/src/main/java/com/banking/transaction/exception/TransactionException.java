package com.banking.transaction.exception;

/**
 * Base Transaction Exception
 * 
 * Parent exception for all transaction-related business exceptions.
 */
public class TransactionException extends RuntimeException {
    
    public TransactionException(String message) {
        super(message);
    }

    public TransactionException(String message, Throwable cause) {
        super(message, cause);
    }
}
