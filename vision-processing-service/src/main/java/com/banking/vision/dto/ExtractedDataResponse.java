package com.banking.vision.dto;

import com.banking.vision.domain.ConfidenceLevel;
import com.banking.vision.domain.DocumentType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

/**
 * Response DTO for extracted structured data.
 * 
 * Returns parsed and structured data extracted from the document
 * based on the document type's extraction template.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExtractedDataResponse {

    /**
     * Document identifier.
     */
    private UUID documentId;

    /**
     * Document type.
     */
    private DocumentType documentType;

    /**
     * Structured extracted data (format varies by document type).
     * 
     * Examples:
     * - RECEIPT: { merchant, date, total, items[], tax, paymentMethod }
     * - INVOICE: { vendor, invoiceNumber, date, lineItems[], subtotal, tax, total }
     * - CHECK: { routingNumber, accountNumber, checkNumber, amount, payee, date }
     */
    private Map<String, Object> extractedData;

    /**
     * Overall confidence score (0-100).
     */
    private Double confidenceScore;

    /**
     * Confidence level classification.
     */
    private ConfidenceLevel confidenceLevel;

    /**
     * Raw OCR text (for reference/debugging).
     */
    private String rawText;

    /**
     * Validation warnings (if any fields failed validation).
     */
    private Map<String, String> validationWarnings;
}
