package com.banking.vision.dto;

import com.banking.vision.domain.DocumentType;
import com.banking.vision.domain.ProcessingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for document upload.
 * 
 * Returns document ID and initial processing status.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentUploadResponse {

    /**
     * Unique document identifier.
     */
    private UUID documentId;

    /**
     * Document type.
     */
    private DocumentType documentType;

    /**
     * Original filename.
     */
    private String filename;

    /**
     * File size in bytes.
     */
    private Long fileSize;

    /**
     * Current processing status.
     */
    private ProcessingStatus status;

    /**
     * Estimated completion time (for async processing).
     */
    private Instant estimatedCompletionTime;

    /**
     * Upload timestamp.
     */
    private Instant uploadedAt;
}
