package com.banking.account.exception;

/**
 * Account Already Exists Exception
 * 
 * Thrown when attempting to create an account that already exists.
 */
public class AccountAlreadyExistsException extends AccountException {

    public AccountAlreadyExistsException(String accountNumber) {
        super("Account already exists with account number: " + accountNumber);
    }

    public AccountAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
