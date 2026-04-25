package com.banking.fraud.domain;

/**
 * Fraud Alert Status
 * 
 * Defines the lifecycle status of fraud alerts.
 */
public enum AlertStatus {
    /**
     * Alert is open and awaiting review
     */
    OPEN,

    /**
     * Alert is under investigation
     */
    INVESTIGATING,

    /**
     * Alert has been resolved (confirmed fraud)
     */
    RESOLVED,

    /**
     * Alert was a false positive
     */
    FALSE_POSITIVE
}
