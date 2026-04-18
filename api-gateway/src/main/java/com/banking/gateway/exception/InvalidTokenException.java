package com.banking.gateway.exception;

/**
 * Exception thrown when JWT token validation fails.
 * 
 * This exception is thrown in the following scenarios:
 * - Token is malformed or cannot be parsed
 * - Token signature verification fails
 * - Token has expired
 * - Token issuer or audience claims are invalid
 * - Token is missing required claims
 * - RSA public key cannot be loaded for verification
 * 
 * Security Considerations:
 * - Exception messages should not reveal sensitive token details
 * - Stack traces should not be exposed to API consumers
 * - All token validation failures should be logged for security monitoring
 * 
 * @author Banking Platform Team
 * @version 1.0.0
 */
public class InvalidTokenException extends RuntimeException {

    /**
     * Create exception with error message.
     * 
     * @param message Error message describing the validation failure
     */
    public InvalidTokenException(String message) {
        super(message);
    }

    /**
     * Create exception with error message and root cause.
     * 
     * @param message Error message describing the validation failure
     * @param cause Root cause exception (e.g., JWT parsing exception)
     */
    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}