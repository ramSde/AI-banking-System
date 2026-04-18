package com.banking.gateway.exception;

/**
 * Exception thrown when JWT token validation fails.
 * 
 * This exception is thrown in the following scenarios:
 * - Token signature validation fails
 * - Token has expired
 * - Token is malformed or has invalid structure
 * - Token issuer or audience validation fails
 * - Token is missing required claims
 * 
 * The exception message should be safe to expose to clients
 * as it doesn't contain sensitive information.
 * 
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2024-01-01
 */
public class InvalidTokenException extends RuntimeException {

    /**
     * Constructs a new InvalidTokenException with the specified detail message.
     * 
     * @param message the detail message explaining why token validation failed
     */
    public InvalidTokenException(String message) {
        super(message);
    }

    /**
     * Constructs a new InvalidTokenException with the specified detail message and cause.
     * 
     * @param message the detail message explaining why token validation failed
     * @param cause the underlying cause of the token validation failure
     */
    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}