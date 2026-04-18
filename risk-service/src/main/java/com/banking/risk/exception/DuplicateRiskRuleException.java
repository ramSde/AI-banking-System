package com.banking.risk.exception;

/**
 * Exception thrown when attempting to create a risk rule with a duplicate name.
 */
public class DuplicateRiskRuleException extends RiskServiceException {

    public DuplicateRiskRuleException(String ruleName) {
        super(String.format("Risk rule already exists with name: %s", ruleName));
    }
}
