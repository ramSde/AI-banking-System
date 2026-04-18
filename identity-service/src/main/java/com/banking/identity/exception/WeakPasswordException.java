package com.banking.identity.exception;

/**
 * Weak Password Exception
 * 
 * Thrown when a password does not meet the required strength criteria.
 */
public class WeakPasswordException extends IdentityException {

    public WeakPasswordException(String message) {
        super("WEAK_PASSWORD", message);
    }

    public WeakPasswordException(String message, Throwable cause) {
        super("WEAK_PASSWORD", message, cause);
    }
}
