package com.banking.vision.event;

import com.banking.vision.domain.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Event published when a document is uploaded.
 * 
 * Consumed by:
 * - Vision Processing Service (async processing)
 * - Audit Service (tracking)
 * - Analytics Service (usage metrics)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentUploadedEvent {

    /**
     * Event ID (unique).
     */
    @Builder.Default
    private String eventId = UUID.randomUUID().toString();

    /**
     * Event type.
     */
    @Builder.Default
    private String eventType = "DocumentUploaded";

    /**
     * Event schema version.
     */
    @Builder.Default
    private String version = "1.0";

    /**
     * Event occurrence timestamp.
     */
    @Builder.Default
    private Instant occurredAt = Instant.now();

    /**
     * Correlation ID for request tracking.
     */
    private String correlationId;

    /**
     * Event payload.
     */
    private Payload payload;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Payload {
        private UUID documentId;
        private UUID userId;
        private DocumentType documentType;
        private String filename;
        private Long fileSize;
        private String mimeType;
        private String storageKey;
    }
}
