package com.banking.vision.service;

import com.banking.vision.domain.DocumentType;

/**
 * Service interface for document classification.
 * 
 * Determines document type based on content analysis.
 * Uses pattern matching and keyword detection.
 */
public interface DocumentClassifierService {

    /**
     * Classify document based on OCR text.
     * 
     * @param ocrText Extracted text
     * @return Detected document type
     */
    DocumentType classifyDocument(String ocrText);

    /**
     * Verify if document matches expected type.
     * 
     * @param ocrText Extracted text
     * @param expectedType Expected document type
     * @return True if matches, false otherwise
     */
    boolean verifyDocumentType(String ocrText, DocumentType expectedType);

    /**
     * Calculate confidence score for classification.
     * 
     * @param ocrText Extracted text
     * @param documentType Document type
     * @return Confidence score (0-100)
     */
    double calculateClassificationConfidence(String ocrText, DocumentType documentType);
}
