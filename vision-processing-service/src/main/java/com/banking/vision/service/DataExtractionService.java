package com.banking.vision.service;

import com.banking.vision.domain.DocumentType;

import java.util.Map;

/**
 * Service interface for structured data extraction.
 * 
 * Extracts structured data from OCR text based on document type
 * and extraction templates.
 */
public interface DataExtractionService {

    /**
     * Extract structured data from OCR text.
     * 
     * @param ocrText Raw OCR text
     * @param documentType Document type
     * @return Extracted structured data
     */
    Map<String, Object> extractData(String ocrText, DocumentType documentType);

    /**
     * Extract receipt data.
     * 
     * @param ocrText Raw OCR text
     * @return Receipt data map
     */
    Map<String, Object> extractReceiptData(String ocrText);

    /**
     * Extract invoice data.
     * 
     * @param ocrText Raw OCR text
     * @return Invoice data map
     */
    Map<String, Object> extractInvoiceData(String ocrText);

    /**
     * Extract check data.
     * 
     * @param ocrText Raw OCR text
     * @return Check data map
     */
    Map<String, Object> extractCheckData(String ocrText);

    /**
     * Extract bank statement data.
     * 
     * @param ocrText Raw OCR text
     * @return Bank statement data map
     */
    Map<String, Object> extractBankStatementData(String ocrText);

    /**
     * Extract ID document data.
     * 
     * @param ocrText Raw OCR text
     * @return ID document data map
     */
    Map<String, Object> extractIdDocumentData(String ocrText);

    /**
     * Validate extracted data against rules.
     * 
     * @param extractedData Extracted data
     * @param documentType Document type
     * @return Validation warnings (empty if valid)
     */
    Map<String, String> validateExtractedData(
        Map<String, Object> extractedData,
        DocumentType documentType
    );
}
