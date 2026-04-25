package com.banking.document.exception;

import java.util.UUID;

public class DocumentNotFoundException extends DocumentException {

    public DocumentNotFoundException(UUID documentId) {
        super(String.format("Document not found with ID: %s", documentId));
    }

    public DocumentNotFoundException(String message) {
        super(message);
    }
}
