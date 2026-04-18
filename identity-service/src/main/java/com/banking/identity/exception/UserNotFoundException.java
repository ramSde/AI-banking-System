package com.banking.identity.exception;

/**
 * User Not Found Exception
 * 
 * Thrown when a user cannot be found by the specified criteria.
 */
public class UserNotFoundException extends IdentityException {

    public UserNotFoundException(String message) {
        super("USER_NOT_FOUND", message);
    }

    public UserNotFoundException(String message, Throwable cause) {
        super("USER_NOT_FOUND", message, cause);
    }
}
