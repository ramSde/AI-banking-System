package com.banking.account.exception;

import java.math.BigDecimal;

/**
 * Insufficient Balance Exception
 * 
 * Thrown when an operation requires more balance than available.
 */
public class InsufficientBalanceException extends AccountException {

    public InsufficientBalanceException(BigDecimal required, BigDecimal available) {
        super(String.format("Insufficient balance. Required: %s, Available: %s", required, available));
    }

    public InsufficientBalanceException(String message) {
        super(message);
    }
}
