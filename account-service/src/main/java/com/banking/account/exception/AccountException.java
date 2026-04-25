package com.banking.account.exception;

/**
 * Base Account Exception
 * 
 * Parent exception for all account-related exceptions.
 */
public class AccountException extends RuntimeException {

    public AccountException(String message) {
        super(message);
    }

    public AccountException(String message, Throwable cause) {
        super(message, cause);
    }
}
