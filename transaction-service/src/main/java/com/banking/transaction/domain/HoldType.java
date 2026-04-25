package com.banking.transaction.domain;

/**
 * Hold Type Enumeration
 * 
 * Defines the types of holds that can be placed on account balances.
 */
public enum HoldType {
    /**
     * Authorization hold - pre-authorization for future transaction
     */
    AUTHORIZATION,
    
    /**
     * Reservation - amount reserved for specific purpose
     */
    RESERVATION,
    
    /**
     * Pending - hold for pending transaction
     */
    PENDING
}
