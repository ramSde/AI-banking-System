package com.banking.fraud.domain;

/**
 * Risk Level Classification
 * 
 * Defines risk levels for fraud checks.
 */
public enum RiskLevel {
    /**
     * Low risk (score 0-30)
     */
    LOW,

    /**
     * Medium risk (score 31-70)
     */
    MEDIUM,

    /**
     * High risk (score 71-100)
     */
    HIGH;

    /**
     * Determine risk level from score
     * 
     * @param score Risk score (0-100)
     * @param mediumThreshold Medium risk threshold
     * @param highThreshold High risk threshold
     * @return Risk level
     */
    public static RiskLevel fromScore(int score, int mediumThreshold, int highThreshold) {
        if (score >= highThreshold) {
            return HIGH;
        } else if (score >= mediumThreshold) {
            return MEDIUM;
        } else {
            return LOW;
        }
    }
}
