package com.banking.vision.service;

import com.banking.vision.domain.DocumentType;
import com.banking.vision.domain.ProcessingStatus;
import com.banking.vision.domain.VisionDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Main service interface for vision processing operations.
 * 
 * Orchestrates:
 * - Document upload and validation
 * - Processing workflow coordination
 * - Status tracking
 * - Result retrieval
 */
public interface VisionService {

    /**
     * Upload and process a document.
     * 
     * @param file Uploaded file
     * @param documentType Type of document
     * @param userId User ID
     * @param metadata Optional metadata
     * @return Created vision document
     */
    VisionDocument uploadDocument(
        MultipartFile file,
        DocumentType documentType,
        UUID userId,
        Map<String, Object> metadata
    );

    /**
     * Get document by ID (user-scoped).
     * 
     * @param documentId Document ID
     * @param userId User ID
     * @return Vision document
     */
    VisionDocument getDocument(UUID documentId, UUID userId);

    /**
     * Get all documents for a user (paginated).
     * 
     * @param userId User ID
     * @param pageable Pagination parameters
     * @return Page of documents
     */
    Page<VisionDocument> getUserDocuments(UUID userId, Pageable pageable);

    /**
     * Get documents by type for a user.
     * 
     * @param userId User ID
     * @param documentType Document type
     * @param pageable Pagination parameters
     * @return Page of documents
     */
    Page<VisionDocument> getUserDocumentsByType(
        UUID userId,
        DocumentType documentType,
        Pageable pageable
    );

    /**
     * Get documents by status for a user.
     * 
     * @param userId User ID
     * @param status Processing status
     * @param pageable Pagination parameters
     * @return Page of documents
     */
    Page<VisionDocument> getUserDocumentsByStatus(
        UUID userId,
        ProcessingStatus status,
        Pageable pageable
    );

    /**
     * Get documents within date range.
     * 
     * @param userId User ID
     * @param startDate Start date
     * @param endDate End date
     * @param pageable Pagination parameters
     * @return Page of documents
     */
    Page<VisionDocument> getUserDocumentsByDateRange(
        UUID userId,
        Instant startDate,
        Instant endDate,
        Pageable pageable
    );

    /**
     * Process a document (async).
     * 
     * @param documentId Document ID
     */
    void processDocument(UUID documentId);

    /**
     * Get processing status.
     * 
     * @param documentId Document ID
     * @param userId User ID
     * @return Processing status details
     */
    Map<String, Object> getProcessingStatus(UUID documentId, UUID userId);

    /**
     * Get OCR results.
     * 
     * @param documentId Document ID
     * @param userId User ID
     * @return OCR results
     */
    Map<String, Object> getOcrResults(UUID documentId, UUID userId);

    /**
     * Get extracted structured data.
     * 
     * @param documentId Document ID
     * @param userId User ID
     * @return Extracted data
     */
    Map<String, Object> getExtractedData(UUID documentId, UUID userId);

    /**
     * Delete a document (soft delete).
     * 
     * @param documentId Document ID
     * @param userId User ID
     */
    void deleteDocument(UUID documentId, UUID userId);

    /**
     * Validate uploaded file.
     * 
     * @param file Uploaded file
     * @throws com.banking.vision.exception.InvalidDocumentException if validation fails
     */
    void validateFile(MultipartFile file);
}
