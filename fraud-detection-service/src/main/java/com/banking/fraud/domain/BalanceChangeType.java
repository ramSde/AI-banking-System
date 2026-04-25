package com.banking.fraud.domain;

/**
 * Balance Change Type
 * 
 * Defines types of balance changes for fraud analysis.
 */
public enum BalanceChangeType {
    /**
     * Credit (money added)
     */
    CREDIT,

    /**
     * Debit (money removed)
     */
    DEBIT,

    /**
     * Refund
     */
    REFUND,

    /**
     * Fee
     */
    FEE,

    /**
     * Adjustment
     */
    ADJUSTMENT
}
