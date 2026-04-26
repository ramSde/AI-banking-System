package com.banking.insight.exception;

public class InsufficientDataException extends InsightException {

    public InsufficientDataException(final String message) {
        super(message);
    }

    public InsufficientDataException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
