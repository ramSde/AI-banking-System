package com.banking.stt.dto;

import com.banking.stt.domain.TranscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for transcription details.
 * Contains complete transcription information including text and metadata.
 *
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2026-05-02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranscriptionResponse {

    /**
     * Transcription ID
     */
    private UUID id;

    /**
     * Audio file ID
     */
    private UUID audioFileId;

    /**
     * Original filename
     */
    private String filename;

    /**
     * Transcription status
     */
    private TranscriptionStatus status;

    /**
     * Detected language code
     */
    private String languageDetected;

    /**
     * Overall confidence score (0-100)
     */
    private BigDecimal confidenceScore;

    /**
     * Complete transcribed text
     */
    private String fullText;

    /**
     * Word count
     */
    private Integer wordCount;

    /**
     * Processing time in seconds
     */
    private Double processingTimeSeconds;

    /**
     * Model used for transcription
     */
    private String modelUsed;

    /**
     * Number of segments
     */
    private Integer segmentCount;

    /**
     * Number of speakers detected (if diarization enabled)
     */
    private Integer speakerCount;

    /**
     * List of speaker information (if diarization enabled)
     */
    private List<SpeakerInfoResponse> speakers;

    /**
     * Error message (if failed)
     */
    private String errorMessage;

    /**
     * Creation timestamp
     */
    private Instant createdAt;

    /**
     * Completion timestamp
     */
    private Instant completedAt;
}
