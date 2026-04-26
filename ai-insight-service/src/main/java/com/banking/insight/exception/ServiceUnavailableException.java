package com.banking.insight.exception;

public class ServiceUnavailableException extends InsightException {

    public ServiceUnavailableException(final String message) {
        super(message);
    }

    public ServiceUnavailableException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
