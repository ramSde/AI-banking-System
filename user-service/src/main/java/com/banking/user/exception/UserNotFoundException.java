package com.banking.user.exception;

/**
 * Exception thrown when a user is not found.
 */
public class UserNotFoundException extends UserException {
    
    public UserNotFoundException(String message) {
        super(message);
    }
}
