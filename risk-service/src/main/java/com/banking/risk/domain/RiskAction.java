package com.banking.risk.domain;

/**
 * Risk action enumeration for adaptive authentication.
 * Defines the action to take based on calculated risk level.
 */
public enum RiskAction {
    /**
     * Allow authentication to proceed without additional steps
     */
    ALLOW,
    
    /**
     * Require multi-factor authentication before allowing access
     */
    REQUIRE_MFA,
    
    /**
     * Block authentication attempt and alert security team
     */
    BLOCK
}
