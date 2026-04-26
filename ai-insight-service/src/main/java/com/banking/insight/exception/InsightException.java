package com.banking.insight.exception;

public class InsightException extends RuntimeException {

    public InsightException(final String message) {
        super(message);
    }

    public InsightException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
