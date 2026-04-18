package com.banking.identity.exception;

/**
 * Invalid Credentials Exception
 * 
 * Thrown when authentication fails due to invalid credentials.
 */
public class InvalidCredentialsException extends IdentityException {

    public InvalidCredentialsException(String message) {
        super("INVALID_CREDENTIALS", message);
    }

    public InvalidCredentialsException(String message, Throwable cause) {
        super("INVALID_CREDENTIALS", message, cause);
    }
}
