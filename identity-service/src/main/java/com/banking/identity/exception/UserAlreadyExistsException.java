package com.banking.identity.exception;

/**
 * User Already Exists Exception
 * 
 * Thrown when attempting to register a user with an email/username that already exists.
 */
public class UserAlreadyExistsException extends IdentityException {

    public UserAlreadyExistsException(String message) {
        super("USER_ALREADY_EXISTS", message);
    }

    public UserAlreadyExistsException(String message, Throwable cause) {
        super("USER_ALREADY_EXISTS", message, cause);
    }
}
