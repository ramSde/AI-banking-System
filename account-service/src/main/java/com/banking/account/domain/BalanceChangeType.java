package com.banking.account.domain;

/**
 * Balance Change Type Enumeration
 * 
 * Defines the types of balance changes that can occur on an account.
 */
public enum BalanceChangeType {
    /**
     * Credit - money added to account
     */
    CREDIT,
    
    /**
     * Debit - money removed from account
     */
    DEBIT,
    
    /**
     * Hold - amount placed on hold (reduces available balance)
     */
    HOLD,
    
    /**
     * Release - hold released (increases available balance)
     */
    RELEASE,
    
    /**
     * Adjustment - manual balance adjustment by admin
     */
    ADJUSTMENT,
    
    /**
     * Interest - interest credited to account
     */
    INTEREST
}
