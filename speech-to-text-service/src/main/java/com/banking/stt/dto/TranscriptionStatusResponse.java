package com.banking.stt.dto;

import com.banking.stt.domain.TranscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for transcription status check.
 * Provides quick status information without full transcription data.
 *
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2026-05-02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranscriptionStatusResponse {

    /**
     * Transcription ID
     */
    private UUID transcriptionId;

    /**
     * Current status
     */
    private TranscriptionStatus status;

    /**
     * Progress percentage (0-100)
     */
    private Integer progressPercentage;

    /**
     * Estimated time remaining in seconds
     */
    private Integer estimatedTimeRemainingSeconds;

    /**
     * Error message (if failed)
     */
    private String errorMessage;

    /**
     * Started at timestamp
     */
    private Instant startedAt;

    /**
     * Completed at timestamp
     */
    private Instant completedAt;

    /**
     * Message to user
     */
    private String message;
}
