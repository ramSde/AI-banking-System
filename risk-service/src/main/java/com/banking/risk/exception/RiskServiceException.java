package com.banking.risk.exception;

/**
 * Base exception for all risk service exceptions.
 * Provides a common parent for all domain-specific exceptions.
 */
public class RiskServiceException extends RuntimeException {

    public RiskServiceException(String message) {
        super(message);
    }

    public RiskServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
