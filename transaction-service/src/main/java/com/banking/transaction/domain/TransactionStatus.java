package com.banking.transaction.domain;

/**
 * Transaction Status Enumeration
 * 
 * Defines the lifecycle states of a transaction.
 */
public enum TransactionStatus {
    /**
     * Transaction initiated but not yet processed
     */
    PENDING,
    
    /**
     * Transaction is being processed
     */
    PROCESSING,
    
    /**
     * Transaction completed successfully
     */
    COMPLETED,
    
    /**
     * Transaction failed
     */
    FAILED,
    
    /**
     * Transaction was reversed
     */
    REVERSED
}
