package com.banking.account.exception;

import java.util.UUID;

/**
 * Account Frozen Exception
 * 
 * Thrown when attempting to perform operations on a frozen account.
 */
public class AccountFrozenException extends AccountException {

    public AccountFrozenException(UUID accountId) {
        super("Account is frozen: " + accountId);
    }

    public AccountFrozenException(String message) {
        super(message);
    }
}
