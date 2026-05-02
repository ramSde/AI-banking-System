package com.banking.vision.exception;

/**
 * Exception thrown when a document fails validation.
 * 
 * This can occur when:
 * - File size exceeds limit
 * - File is empty or corrupted
 * - Required metadata is missing
 * - Document format is invalid
 */
public class InvalidDocumentException extends VisionException {

    public InvalidDocumentException(String message) {
        super(message, "INVALID_DOCUMENT");
    }

    public InvalidDocumentException(String message, String errorCode) {
        super(message, errorCode);
    }

    public InvalidDocumentException(String message, Throwable cause) {
        super(message, "INVALID_DOCUMENT", cause);
    }
}
