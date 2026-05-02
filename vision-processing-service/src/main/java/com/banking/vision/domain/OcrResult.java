package com.banking.vision.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

/**
 * OCR Result entity storing raw text extraction results.
 * 
 * Contains the raw OCR output from Tesseract, confidence scores,
 * and processing metadata. Multiple results can exist for multi-page documents.
 */
@Entity
@Table(name = "ocr_results", indexes = {
    @Index(name = "idx_ocr_results_document_id", columnList = "document_id")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OcrResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "document_id", nullable = false)
    private UUID documentId;

    @Column(name = "raw_text", nullable = false, columnDefinition = "TEXT")
    private String rawText;

    @Column(name = "confidence_score", precision = 5, scale = 2)
    private Double confidenceScore;

    @Column(name = "language_detected", length = 10)
    private String languageDetected;

    @Column(name = "page_number", nullable = false)
    @Builder.Default
    private Integer pageNumber = 1;

    @Column(name = "processing_time_ms")
    private Long processingTimeMs;

    @Column(name = "ocr_engine", nullable = false, length = 50)
    @Builder.Default
    private String ocrEngine = "Tesseract";

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;

    /**
     * Get confidence level classification.
     */
    public ConfidenceLevel getConfidenceLevel() {
        if (confidenceScore == null) {
            return ConfidenceLevel.LOW;
        }
        return ConfidenceLevel.fromScore(confidenceScore);
    }

    /**
     * Check if OCR result has high confidence.
     */
    public boolean isHighConfidence() {
        return getConfidenceLevel() == ConfidenceLevel.HIGH;
    }
}
