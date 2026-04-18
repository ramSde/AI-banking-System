package com.banking.identity.exception;

/**
 * Base Identity Exception
 * 
 * Base exception class for all identity service exceptions.
 */
public class IdentityException extends RuntimeException {

    private final String errorCode;

    public IdentityException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public IdentityException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
