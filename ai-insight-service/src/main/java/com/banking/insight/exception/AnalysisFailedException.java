package com.banking.insight.exception;

public class AnalysisFailedException extends InsightException {

    public AnalysisFailedException(final String message) {
        super(message);
    }

    public AnalysisFailedException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
