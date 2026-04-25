package com.banking.user.exception;

/**
 * Base exception for all user-related errors.
 */
public class UserException extends RuntimeException {
    
    public UserException(String message) {
        super(message);
    }

    public UserException(String message, Throwable cause) {
        super(message, cause);
    }
}
