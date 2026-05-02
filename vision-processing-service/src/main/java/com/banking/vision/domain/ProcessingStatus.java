package com.banking.vision.domain;

/**
 * Processing status lifecycle for vision documents.
 * 
 * Status flow:
 * PENDING → PROCESSING → COMPLETED (success)
 *                      → FAILED (error)
 */
public enum ProcessingStatus {
    /**
     * Document uploaded, queued for processing
     */
    PENDING,
    
    /**
     * OCR and extraction in progress
     */
    PROCESSING,
    
    /**
     * Processing completed successfully
     */
    COMPLETED,
    
    /**
     * Processing failed with error
     */
    FAILED
}
