package com.banking.vision.dto;

import com.banking.vision.domain.ConfidenceLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Response DTO for OCR results.
 * 
 * Returns raw text extraction results with confidence scores.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OcrResultResponse {

    /**
     * Document identifier.
     */
    private UUID documentId;

    /**
     * OCR results per page.
     */
    private List<PageResult> pages;

    /**
     * Overall confidence score (average across all pages).
     */
    private Double overallConfidenceScore;

    /**
     * Overall confidence level.
     */
    private ConfidenceLevel overallConfidenceLevel;

    /**
     * Total processing time in milliseconds.
     */
    private Long totalProcessingTimeMs;

    /**
     * OCR result for a single page.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PageResult {
        /**
         * Page number (1-indexed).
         */
        private Integer pageNumber;

        /**
         * Raw extracted text.
         */
        private String rawText;

        /**
         * Confidence score for this page (0-100).
         */
        private Double confidenceScore;

        /**
         * Confidence level for this page.
         */
        private ConfidenceLevel confidenceLevel;

        /**
         * Detected language code (ISO 639-1).
         */
        private String languageDetected;

        /**
         * Processing time for this page in milliseconds.
         */
        private Long processingTimeMs;
    }
}
