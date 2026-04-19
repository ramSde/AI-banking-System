package com.banking.device.domain;

/**
 * Enumeration of anomaly severity levels for risk assessment.
 * Determines the urgency and response required for detected anomalies.
 */
public enum Severity {
    /**
     * Low severity - informational, minimal risk
     */
    LOW,
    
    /**
     * Medium severity - moderate risk, monitoring required
     */
    MEDIUM,
    
    /**
     * High severity - significant risk, immediate attention needed
     */
    HIGH,
    
    /**
     * Critical severity - extreme risk, immediate action required
     */
    CRITICAL
}