package com.banking.risk.exception;

/**
 * Exception thrown when risk assessment fails.
 */
public class RiskAssessmentException extends RiskServiceException {

    public RiskAssessmentException(String message) {
        super(message);
    }

    public RiskAssessmentException(String message, Throwable cause) {
        super(message, cause);
    }
}
