package com.banking.account.exception;

import java.util.UUID;

/**
 * Account Not Found Exception
 * 
 * Thrown when an account cannot be found by ID or account number.
 */
public class AccountNotFoundException extends AccountException {

    public AccountNotFoundException(UUID accountId) {
        super("Account not found with ID: " + accountId);
    }

    public AccountNotFoundException(String accountNumber) {
        super("Account not found with account number: " + accountNumber);
    }

    public AccountNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
