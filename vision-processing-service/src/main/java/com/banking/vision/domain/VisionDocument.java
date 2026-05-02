package com.banking.vision.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Vision Document entity representing uploaded documents for OCR processing.
 * 
 * Stores metadata about documents, processing status, and references to
 * storage locations. Actual file content is stored in MinIO/S3.
 */
@Entity
@Table(name = "vision_documents", indexes = {
    @Index(name = "idx_vision_documents_user_id", columnList = "user_id"),
    @Index(name = "idx_vision_documents_status", columnList = "processing_status"),
    @Index(name = "idx_vision_documents_type", columnList = "document_type"),
    @Index(name = "idx_vision_documents_created", columnList = "created_at")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VisionDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 50)
    private DocumentType documentType;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    @Column(name = "storage_key", nullable = false, length = 500)
    private String storageKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "processing_status", nullable = false, length = 50)
    @Builder.Default
    private ProcessingStatus processingStatus = ProcessingStatus.PENDING;

    @Column(name = "confidence_score", precision = 5, scale = 2)
    private Double confidenceScore;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

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
     * Check if document is soft-deleted.
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * Soft delete the document.
     */
    public void softDelete() {
        this.deletedAt = Instant.now();
    }

    /**
     * Check if processing is complete (success or failure).
     */
    public boolean isProcessingComplete() {
        return processingStatus == ProcessingStatus.COMPLETED 
            || processingStatus == ProcessingStatus.FAILED;
    }

    /**
     * Mark processing as started.
     */
    public void startProcessing() {
        this.processingStatus = ProcessingStatus.PROCESSING;
    }

    /**
     * Mark processing as completed successfully.
     */
    public void completeProcessing(Double confidenceScore) {
        this.processingStatus = ProcessingStatus.COMPLETED;
        this.confidenceScore = confidenceScore;
        this.errorMessage = null;
    }

    /**
     * Mark processing as failed.
     */
    public void failProcessing(String errorMessage) {
        this.processingStatus = ProcessingStatus.FAILED;
        this.errorMessage = errorMessage;
    }
}
