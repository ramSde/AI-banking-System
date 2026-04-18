package com.banking.identity.exception;

/**
 * Invalid Token Exception
 * 
 * Thrown when a JWT or refresh token is invalid, expired, or revoked.
 */
public class InvalidTokenException extends IdentityException {

    public InvalidTokenException(String message) {
        super("INVALID_TOKEN", message);
    }

    public InvalidTokenException(String message, Throwable cause) {
        super("INVALID_TOKEN", message, cause);
    }
}
