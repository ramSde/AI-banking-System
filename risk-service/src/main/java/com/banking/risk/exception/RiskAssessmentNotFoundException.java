package com.banking.risk.exception;

import java.util.UUID;

/**
 * Exception thrown when a risk assessment is not found.
 */
public class RiskAssessmentNotFoundException extends RiskServiceException {

    public RiskAssessmentNotFoundException(UUID assessmentId) {
        super(String.format("Risk assessment not found with ID: %s", assessmentId));
    }

    public RiskAssessmentNotFoundException(String message) {
        super(message);
    }
}
