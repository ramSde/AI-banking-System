package com.banking.stt.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Event published when a transcription fails.
 *
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2026-05-02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranscriptionFailedEvent {

    /**
     * Unique event ID
     */
    @Builder.Default
    private UUID eventId = UUID.randomUUID();

    /**
     * Event type
     */
    @Builder.Default
    private String eventType = "TranscriptionFailed";

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
     * Error message
     */
    private String errorMessage;

    /**
     * Error code
     */
    private String errorCode;

    /**
     * Retry count
     */
    private Integer retryCount;
}
