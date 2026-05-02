package com.banking.stt.dto;

import com.banking.stt.domain.TranscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for audio file upload.
 * Contains information about the uploaded audio and transcription job.
 *
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2026-05-02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AudioUploadResponse {

    /**
     * Audio file ID
     */
    private UUID audioFileId;

    /**
     * Transcription ID
     */
    private UUID transcriptionId;

    /**
     * Original filename
     */
    private String filename;

    /**
     * File size in bytes
     */
    private Long fileSizeBytes;

    /**
     * Audio duration in seconds
     */
    private Double durationSeconds;

    /**
     * Audio format
     */
    private String format;

    /**
     * Language code (detected or specified)
     */
    private String languageCode;

    /**
     * Current transcription status
     */
    private TranscriptionStatus status;

    /**
     * Estimated completion time in seconds
     */
    private Integer estimatedCompletionSeconds;

    /**
     * Upload timestamp
     */
    private Instant uploadedAt;

    /**
     * Message to user
     */
    private String message;
}
