package com.banking.otp.domain;

/**
 * Enumeration of MFA enrollment status
 */
public enum MfaStatus {
    /**
     * MFA is active and can be used
     */
    ACTIVE,
    
    /**
     * MFA has been disabled by user
     */
    DISABLED,
    
    /**
     * MFA has been suspended (e.g., due to security concerns)
     */
    SUSPENDED
}
