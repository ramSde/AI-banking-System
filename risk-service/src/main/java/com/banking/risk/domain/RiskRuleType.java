package com.banking.risk.domain;

/**
 * Risk rule type enumeration.
 * Categorizes different types of risk assessment rules.
 */
public enum RiskRuleType {
    /**
     * Device-based risk rules (new device, device trust score)
     */
    DEVICE,
    
    /**
     * Location-based risk rules (new location, impossible travel)
     */
    LOCATION,
    
    /**
     * Velocity-based risk rules (multiple attempts in short time)
     */
    VELOCITY,
    
    /**
     * Time-based risk rules (unusual hours, time patterns)
     */
    TIME,
    
    /**
     * Failed attempts tracking rules
     */
    FAILED_ATTEMPTS,
    
    /**
     * Custom rules defined by administrators
     */
    CUSTOM
}
