package com.banking.identity.domain;

/**
 * User Status Enum
 * 
 * Represents the current status of a user account.
 */
public enum UserStatus {
    /**
     * Account is active and can be used normally
     */
    ACTIVE,

    /**
     * Account is inactive (user-initiated or system-initiated)
     */
    INACTIVE,

    /**
     * Account is suspended (administrative action, temporary)
     */
    SUSPENDED,

    /**
     * Account is locked (security measure, temporary)
     */
    LOCKED
}
