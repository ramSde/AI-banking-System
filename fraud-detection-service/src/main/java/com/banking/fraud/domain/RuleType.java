package com.banking.fraud.domain;

/**
 * Fraud Rule Types
 * 
 * Defines the types of fraud detection rules available.
 */
public enum RuleType {
    /**
     * Velocity-based rules (transaction frequency)
     */
    VELOCITY,

    /**
     * Amount-based rules (transaction amounts)
     */
    AMOUNT,

    /**
     * Geographic anomaly detection
     */
    GEOGRAPHIC,

    /**
     * Time pattern analysis
     */
    TIME_PATTERN,

    /**
     * Account age-based rules
     */
    ACCOUNT_AGE,

    /**
     * Failed attempt tracking
     */
    FAILED_ATTEMPTS,

    /**
     * Device-based rules
     */
    DEVICE,

    /**
     * Behavioral pattern analysis
     */
    BEHAVIORAL,

    /**
     * Custom rule type
     */
    CUSTOM
}
