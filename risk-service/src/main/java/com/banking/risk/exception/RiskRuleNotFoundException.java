package com.banking.risk.exception;

import java.util.UUID;

/**
 * Exception thrown when a risk rule is not found.
 */
public class RiskRuleNotFoundException extends RiskServiceException {

    public RiskRuleNotFoundException(UUID ruleId) {
        super(String.format("Risk rule not found with ID: %s", ruleId));
    }

    public RiskRuleNotFoundException(String message) {
        super(message);
    }
}
