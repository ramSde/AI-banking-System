package com.banking.chat.exception;

public class RateLimitExceededException extends ChatException {

    public RateLimitExceededException(String message) {
        super("RATE_LIMIT_EXCEEDED", message);
    }

    public RateLimitExceededException(String message, Throwable cause) {
        super("RATE_LIMIT_EXCEEDED", message, cause);
    }
}
