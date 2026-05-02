package com.banking.stt.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Event published when a transcription is completed successfully.
 *
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2026-05-02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranscriptionCompletedEvent {

    /**
     * Unique event ID
     */
    @Builder.Default
    private UUID eventId = UUID.randomUUID();

    /**
     * Event type
     */
    @Builder.Default
    private String eventType = "TranscriptionCompleted";

    /**
     * Event version
     */
    @Builder.Default
    private String version = "1.0";

    /**
     * When the event occurred
     */
    @Builder.Default
    private Instant occurredAt = Instant.now();

    /**
     * Correlation ID for tracing
     */
    private UUID correlationId;

    /**
     * Transcription ID
     */
    private UUID transcriptionId;

    /**
     * Audio file ID
     */
    private UUID audioFileId;

    /**
     * User ID
     */
    private UUID userId;

    /**
     * Detected language
     */
    private String languageDetected;

    /**
     * Word count
     */
    private Integer wordCount;

    /**
     * Processing time in milliseconds
     */
    private Long processingTimeMs;

    /**
     * Model used
     */
    private String modelUsed;
}
