package com.banking.vision.exception;

/**
 * Exception thrown when OCR processing fails.
 * 
 * This can occur when:
 * - Tesseract engine fails
 * - Image quality is too poor
 * - Document is unreadable
 * - Processing timeout
 */
public class OcrProcessingException extends VisionException {

    public OcrProcessingException(String message) {
        super(message, "OCR_PROCESSING_FAILED");
    }

    public OcrProcessingException(String message, Throwable cause) {
        super(message, "OCR_PROCESSING_FAILED", cause);
    }

    public OcrProcessingException(String message, String errorCode) {
        super(message, errorCode);
    }

    public OcrProcessingException(String message, String errorCode, Throwable cause) {
        super(message, errorCode, cause);
    }
}
