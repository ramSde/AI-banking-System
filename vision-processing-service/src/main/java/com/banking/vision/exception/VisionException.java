package com.banking.vision.exception;

/**
 * Base exception for all vision processing service errors.
 * 
 * All domain-specific exceptions extend this base class.
 */
public class VisionException extends RuntimeException {

    private final String errorCode;

    public VisionException(String message) {
        super(message);
        this.errorCode = "VISION_ERROR";
    }

    public VisionException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public VisionException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "VISION_ERROR";
    }

    public VisionException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
