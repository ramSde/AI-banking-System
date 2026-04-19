package com.banking.user.domain;

/**
 * Enumeration of user account status values.
 * Determines user's ability to access banking services.
 */
public enum UserStatus {
    /**
     * User account is active and can access all services
     */
    ACTIVE,
    
    /**
     * User account is temporarily suspended
     */
    SUSPENDED,
    
    /**
     * User account is permanently closed
     */
    CLOSED,
    
    /**
     * User account is pending activation
     */
    PENDING,
    
    /**
     * User account is locked due to security concerns
     */
    LOCKED
}
