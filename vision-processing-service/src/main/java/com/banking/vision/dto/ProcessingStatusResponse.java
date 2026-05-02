package com.banking.vision.dto;

import com.banking.vision.domain.ConfidenceLevel;
import com.banking.vision.domain.ProcessingStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Response DTO for processing status check.
 * 
 * Returns current status, confidence, and error details if failed.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProcessingStatusResponse {

    /**
     * Document identifier.
     */
    private UUID documentId;

    /**
     * Current processing status.
     */
    private ProcessingStatus status;

    /**
     * Overall confidence score (0-100).
     */
    private Double confidenceScore;

    /**
     * Confidence level classification.
     */
    private ConfidenceLevel confidenceLevel;

    /**
     * Processing time in milliseconds.
     */
    private Long processingTimeMs;

    /**
     * Number of pages processed.
     */
    private Integer pageCount;

    /**
     * Error message (if status is FAILED).
     */
    private String errorMessage;
}
