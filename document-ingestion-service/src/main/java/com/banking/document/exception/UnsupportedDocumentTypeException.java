package com.banking.document.exception;

public class UnsupportedDocumentTypeException extends DocumentException {

    public UnsupportedDocumentTypeException(String mimeType) {
        super(String.format("Unsupported document type: %s", mimeType));
    }

    public UnsupportedDocumentTypeException(String message, Throwable cause) {
        super(message, cause);
    }
}
