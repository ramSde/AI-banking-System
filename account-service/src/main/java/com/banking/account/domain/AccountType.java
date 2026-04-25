package com.banking.account.domain;

/**
 * Account Type Enumeration
 * 
 * Defines the types of accounts supported by the banking platform.
 */
public enum AccountType {
    /**
     * Savings account - typically earns interest, may have withdrawal limits
     */
    SAVINGS,
    
    /**
     * Checking account - for daily transactions, may have overdraft protection
     */
    CHECKING,
    
    /**
     * Credit account - allows negative balance up to credit limit
     */
    CREDIT
}
