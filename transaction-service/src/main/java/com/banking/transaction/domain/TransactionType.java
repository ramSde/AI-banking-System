package com.banking.transaction.domain;

/**
 * Transaction Type Enumeration
 * 
 * Defines the types of financial transactions supported.
 */
public enum TransactionType {
    /**
     * Deposit - money added to account
     */
    DEPOSIT,
    
    /**
     * Withdrawal - money removed from account
     */
    WITHDRAWAL,
    
    /**
     * Transfer - money moved between accounts
     */
    TRANSFER,
    
    /**
     * Payment - payment to external party
     */
    PAYMENT,
    
    /**
     * Refund - money returned to account
     */
    REFUND,
    
    /**
     * Fee - service fee charged
     */
    FEE,
    
    /**
     * Interest - interest credited
     */
    INTEREST,
    
    /**
     * Reversal - transaction reversal
     */
    REVERSAL
}
