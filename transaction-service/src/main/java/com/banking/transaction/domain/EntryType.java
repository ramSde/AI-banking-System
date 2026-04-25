package com.banking.transaction.domain;

/**
 * Ledger Entry Type Enumeration
 * 
 * Defines the type of ledger entry in double-entry bookkeeping.
 */
public enum EntryType {
    /**
     * Debit entry - money removed from account
     */
    DEBIT,
    
    /**
     * Credit entry - money added to account
     */
    CREDIT
}
