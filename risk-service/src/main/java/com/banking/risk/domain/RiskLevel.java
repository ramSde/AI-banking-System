package com.banking.risk.domain;

/**
 * Risk level enumeration for risk assessment results.
 * Determines the severity of risk detected during authentication.
 */
public enum RiskLevel {
    /**
     * Low risk (0-30): Allow authentication without additional verification
     */
    LOW,
    
    /**
     * Medium risk (31-60): Require MFA before allowing authentication
     */
    MEDIUM,
    
    /**
     * High risk (61-100): Block authentication and alert security team
     */
    HIGH
}
