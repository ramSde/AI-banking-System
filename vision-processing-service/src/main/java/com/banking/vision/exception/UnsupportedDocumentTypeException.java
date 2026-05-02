package com.banking.vision.exception;

/**
 * Exception thrown when an unsupported document type or format is uploaded.
 * 
 * This can occur when:
 * - File format is not supported (e.g., .docx, .xlsx)
 * - MIME type is invalid
 * - File is corrupted or unreadable
 */
public class UnsupportedDocumentTypeException extends VisionException {

    public UnsupportedDocumentTypeException(String mimeType) {
        super(
            String.format("Unsupported document type: %s. Supported formats: PDF, PNG, JPG, JPEG, TIFF", mimeType),
            "UNSUPPORTED_DOCUMENT_TYPE"
        );
    }

    public UnsupportedDocumentTypeException(String message, String errorCode) {
        super(message, errorCode);
    }
}
