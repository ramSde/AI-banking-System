package com.banking.transaction.exception;

import java.math.BigDecimal;

/**
 * Transaction Limit Exceeded Exception
 * 
 * Thrown when a transaction exceeds configured limits (amount or daily total).
 */
public class TransactionLimitExceededException extends TransactionException {

    public TransactionLimitExceededException(String limitType, BigDecimal limit, BigDecimal attempted) {
        super(String.format("%s limit exceeded. Limit: %s, Attempted: %s", limitType, limit, attempted));
    }

    public static TransactionLimitExceededException maxAmount(BigDecimal limit, BigDecimal attempted) {
        return new TransactionLimitExceededException("Maximum transaction amount", limit, attempted);
    }

    public static TransactionLimitExceededException dailyLimit(BigDecimal limit, BigDecimal attempted) {
        return new TransactionLimitExceededException("Daily transaction limit", limit, attempted);
    }
}
