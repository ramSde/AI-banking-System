package com.banking.account.exception;

import java.util.UUID;

/**
 * Account Closed Exception
 * 
 * Thrown when attempting to perform operations on a closed account.
 */
public class AccountClosedException extends AccountException {

    public AccountClosedException(UUID accountId) {
        super("Account is closed: " + accountId);
    }

    public AccountClosedException(String message) {
        super(message);
    }
}
