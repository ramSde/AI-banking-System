package com.banking.fraud.exception;

import java.util.UUID;

/**
 * Fraud Rule Not Found Exception
 * 
 * Thrown when a fraud rule is not found.
 */
public class FraudRuleNotFoundException extends FraudException {

    public FraudRuleNotFoundException(UUID ruleId) {
        super("Fraud rule not found with ID: " + ruleId);
    }

    public FraudRuleNotFoundException(String ruleName) {
        super("Fraud rule not found with name: " + ruleName);
    }
}
