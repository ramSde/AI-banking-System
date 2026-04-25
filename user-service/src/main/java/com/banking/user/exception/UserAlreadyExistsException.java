package com.banking.user.exception;

/**
 * Exception thrown when attempting to create a user that already exists.
 */
public class UserAlreadyExistsException extends UserException {
    
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
