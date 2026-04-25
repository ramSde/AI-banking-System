package com.banking.fraud.exception;

/**
 * Base Fraud Exception
 * 
 * Base exception for all fraud detection related errors.
 */
public class FraudException extends RuntimeException {

    public FraudException(String message) {
        super(message);
    }

    public FraudException(String message, Throwable cause) {
        super(message, cause);
    }
}
