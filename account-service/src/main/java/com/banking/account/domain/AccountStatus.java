package com.banking.account.domain;

/**
 * Account Status Enumeration
 * 
 * Defines the lifecycle states of an account.
 */
public enum AccountStatus {
    /**
     * Account is active and can perform all operations
     */
    ACTIVE,
    
    /**
     * Account is temporarily inactive but can be reactivated
     */
    INACTIVE,
    
    /**
     * Account is frozen due to security or compliance reasons
     */
    FROZEN,
    
    /**
     * Account is permanently closed
     */
    CLOSED
}
