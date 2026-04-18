package com.banking.identity.exception;

/**
 * Account Locked Exception
 * 
 * Thrown when attempting to authenticate with a locked account.
 */
public class AccountLockedException extends IdentityException {

    public AccountLockedException(String message) {
        super("ACCOUNT_LOCKED", message);
    }

    public AccountLockedException(String message, Throwable cause) {
        super("ACCOUNT_LOCKED", message, cause);
    }
}
