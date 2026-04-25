package com.banking.transaction.exception;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Insufficient Balance Exception
 * 
 * Thrown when an account does not have sufficient balance for a transaction.
 */
public class InsufficientBalanceException extends TransactionException {

    public InsufficientBalanceException(UUID accountId, BigDecimal required, BigDecimal available) {
        super(String.format("Insufficient balance in account %s. Required: %s, Available: %s", 
                accountId, required, available));
    }
}
