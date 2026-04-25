package com.banking.account.exception;

/**
 * Invalid Account Operation Exception
 * 
 * Thrown when an invalid operation is attempted on an account.
 */
public class InvalidAccountOperationException extends AccountException {

    public InvalidAccountOperationException(String message) {
        super(message);
    }

    public InvalidAccountOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
