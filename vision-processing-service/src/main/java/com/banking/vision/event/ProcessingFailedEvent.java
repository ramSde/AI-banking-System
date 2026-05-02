package com.banking.vision.event;

import com.banking.vision.domain.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Event published when document processing fails.
 * 
 * Consumed by:
 * - Notification Service (error notification)
 * - Monitoring Service (alerting)
 * - Analytics Service (failure metrics)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessingFailedEvent {

    /**
     * Event ID (unique).
     */
    @Builder.Default
    private String eventId = UUID.randomUUID().toString();

    /**
     * Event type.
     */
    @Builder.Default
    private String eventType = "ProcessingFailed";

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
        private String errorMessage;
        private String errorCode;
        private Long processingTimeMs;
    }
}
