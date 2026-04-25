package com.banking.fraud.exception;

/**
 * Invalid Rule Exception
 * 
 * Thrown when a fraud rule configuration is invalid.
 */
public class InvalidRuleException extends FraudException {

    public InvalidRuleException(String message) {
        super(message);
    }

    public InvalidRuleException(String message, Throwable cause) {
        super(message, cause);
    }
}
