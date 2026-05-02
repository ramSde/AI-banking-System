package com.banking.vision.dto;

import com.banking.vision.domain.DocumentType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for document upload.
 * 
 * Submitted as multipart/form-data with file and metadata.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentUploadRequest {

    /**
     * Type of document being uploaded.
     * Required to select appropriate extraction template.
     */
    @NotNull(message = "Document type is required")
    private DocumentType documentType;

    /**
     * Optional description or notes about the document.
     */
    private String description;

    /**
     * Optional tags for categorization (comma-separated).
     */
    private String tags;
}
