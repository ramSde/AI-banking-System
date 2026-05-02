package com.banking.stt.exception;

/**
 * Base exception for Speech-to-Text Service.
 * All custom exceptions in this service should extend this class.
 *
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2026-05-02
 */
public class SttException extends RuntimeException {

    private final String errorCode;

    public SttException(String message) {
        super(message);
        this.errorCode = "STT_ERROR";
    }

    public SttException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public SttException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "STT_ERROR";
    }

    public SttException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
