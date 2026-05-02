package com.banking.stt.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Event published when an audio file is uploaded.
 *
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2026-05-02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AudioUploadedEvent {

    /**
     * Unique event ID
     */
    @Builder.Default
    private UUID eventId = UUID.randomUUID();

    /**
     * Event type
     */
    @Builder.Default
    private String eventType = "AudioUploaded";

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
     * Audio file ID
     */
    private UUID audioFileId;

    /**
     * User ID who uploaded the file
     */
    private UUID userId;

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
     * Language code
     */
    private String languageCode;
}
