package com.banking.gateway.exception;

/**
 * Invalid Token Exception
 * 
 * Thrown when JWT token validation fails.
 * 
 * Possible Causes:
 * - Token signature verification failed
 * - Token has expired
 * - Token issuer doesn't match expected issuer
 * - Token audience doesn't match expected audience
 * - Token is malformed or corrupted
 * - Required claims are missing
 * 
 * HTTP Response:
 * - Status: 401 Unauthorized
 * - Message: Specific validation failure reason
 * 
 * @author Banking Platform Team
 * @version 1.0.0
 */
public class InvalidTokenException extends RuntimeException {

    public InvalidTokenException(String message) {
        super(message);
    }

    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
