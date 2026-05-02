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
 * Entity representing an uploaded audio file.
 * Stores metadata about audio files uploaded for transcription.
 *
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2026-05-02
 */
@Entity
@Table(name = "audio_files")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AudioFile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * User who uploaded the audio file
     */
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /**
     * Original filename
     */
    @Column(nullable = false, length = 255)
    private String filename;

    /**
     * Original audio format
     */
    @Column(name = "original_format", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private AudioFormat originalFormat;

    /**
     * Format after conversion (if converted)
     */
    @Column(name = "converted_format", length = 10)
    @Enumerated(EnumType.STRING)
    private AudioFormat convertedFormat;

    /**
     * File size in bytes
     */
    @Column(name = "file_size_bytes", nullable = false)
    private Long fileSizeBytes;

    /**
     * Audio duration in seconds
     */
    @Column(name = "duration_seconds", precision = 10, scale = 2)
    private BigDecimal durationSeconds;

    /**
     * Storage path in object storage (S3/MinIO)
     */
    @Column(name = "storage_path", nullable = false, length = 500)
    private String storagePath;

    /**
     * Language code (ISO 639-1)
     */
    @Column(name = "language_code", length = 10)
    private String languageCode;

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
     * Check if the audio file is deleted.
     *
     * @return true if deleted, false otherwise
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * Soft delete the audio file.
     */
    public void softDelete() {
        this.deletedAt = Instant.now();
    }

    /**
     * Get file size in megabytes.
     *
     * @return file size in MB
     */
    public double getFileSizeMB() {
        return fileSizeBytes / (1024.0 * 1024.0);
    }

    /**
     * Get duration in minutes.
     *
     * @return duration in minutes
     */
    public BigDecimal getDurationMinutes() {
        if (durationSeconds == null) {
            return BigDecimal.ZERO;
        }
        return durationSeconds.divide(BigDecimal.valueOf(60), 2, BigDecimal.ROUND_HALF_UP);
    }
}
