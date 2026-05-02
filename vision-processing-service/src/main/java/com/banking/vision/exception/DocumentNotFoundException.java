package com.banking.vision.exception;

import java.util.UUID;

/**
 * Exception thrown when a requested document is not found.
 * 
 * This can occur when:
 * - Document ID doesn't exist
 * - User doesn't have access to the document
 * - Document has been soft-deleted
 */
public class DocumentNotFoundException extends VisionException {

    public DocumentNotFoundException(UUID documentId) {
        super(
            String.format("Document not found with ID: %s", documentId),
            "DOCUMENT_NOT_FOUND"
        );
    }

    public DocumentNotFoundException(UUID documentId, UUID userId) {
        super(
            String.format("Document not found with ID: %s for user: %s", documentId, userId),
            "DOCUMENT_NOT_FOUND"
        );
    }

    public DocumentNotFoundException(String message) {
        super(message, "DOCUMENT_NOT_FOUND");
    }
}
