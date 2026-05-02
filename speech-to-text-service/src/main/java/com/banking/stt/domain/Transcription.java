package com.banking.stt.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Entity representing a transcription result.
 * Stores the output from speech-to-text processing.
 *
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2026-05-02
 */
@Entity
@Table(name = "transcriptions")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transcription {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Reference to the audio file
     */
    @Column(name = "audio_file_id", nullable = false)
    private UUID audioFileId;

    /**
     * User who owns this transcription
     */
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /**
     * Processing status
     */
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TranscriptionStatus status = TranscriptionStatus.PENDING;

    /**
     * Detected language code (ISO 639-1)
     */
    @Column(name = "language_detected", length = 10)
    private String languageDetected;

    /**
     * Overall confidence score (0-100)
     */
    @Column(name = "confidence_score", precision = 5, scale = 2)
    private BigDecimal confidenceScore;

    /**
     * Complete transcribed text
     */
    @Column(name = "full_text", columnDefinition = "TEXT")
    private String fullText;

    /**
     * Total word count
     */
    @Column(name = "word_count")
    private Integer wordCount;

    /**
     * Processing time in milliseconds
     */
    @Column(name = "processing_time_ms")
    private Long processingTimeMs;

    /**
     * Model used for transcription (e.g., whisper-1)
     */
    @Column(name = "model_used", length = 50)
    private String modelUsed;

    /**
     * Error message if transcription failed
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * Creation timestamp
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Last update timestamp
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * Soft delete timestamp
     */
    @Column(name = "deleted_at")
    private Instant deletedAt;

    /**
     * Version for optimistic locking
     */
    @Version
    private Long version;

    /**
     * Check if transcription is completed.
     *
     * @return true if status is COMPLETED
     */
    public boolean isCompleted() {
        return status == TranscriptionStatus.COMPLETED;
    }

    /**
     * Check if transcription failed.
     *
     * @return true if status is FAILED
     */
    public boolean isFailed() {
        return status == TranscriptionStatus.FAILED;
    }

    /**
     * Check if transcription is in progress.
     *
     * @return true if status is PROCESSING
     */
    public boolean isProcessing() {
        return status == TranscriptionStatus.PROCESSING;
    }

    /**
     * Mark transcription as processing.
     */
    public void markAsProcessing() {
        this.status = TranscriptionStatus.PROCESSING;
    }

    /**
     * Mark transcription as completed.
     *
     * @param fullText      Transcribed text
     * @param wordCount     Word count
     * @param processingTime Processing time in milliseconds
     */
    public void markAsCompleted(String fullText, Integer wordCount, Long processingTime) {
        this.status = TranscriptionStatus.COMPLETED;
        this.fullText = fullText;
        this.wordCount = wordCount;
        this.processingTimeMs = processingTime;
    }

    /**
     * Mark transcription as failed.
     *
     * @param errorMessage Error message
     */
    public void markAsFailed(String errorMessage) {
        this.status = TranscriptionStatus.FAILED;
        this.errorMessage = errorMessage;
    }

    /**
     * Check if the transcription is deleted.
     *
     * @return true if deleted, false otherwise
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * Soft delete the transcription.
     */
    public void softDelete() {
        this.deletedAt = Instant.now();
    }

    /**
     * Get processing time in seconds.
     *
     * @return processing time in seconds
     */
    public Double getProcessingTimeSeconds() {
        if (processingTimeMs == null) {
            return null;
        }
        return processingTimeMs / 1000.0;
    }
}
