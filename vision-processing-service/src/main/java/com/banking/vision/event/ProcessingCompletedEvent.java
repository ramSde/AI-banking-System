package com.banking.vision.event;

import com.banking.vision.domain.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Event published when document processing completes successfully.
 * 
 * Consumed by:
 * - Transaction Categorization Service (for receipts)
 * - Notification Service (user notification)
 * - Analytics Service (processing metrics)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessingCompletedEvent {

    /**
     * Event ID (unique).
     */
    @Builder.Default
    private String eventId = UUID.randomUUID().toString();

    /**
     * Event type.
     */
    @Builder.Default
    private String eventType = "ProcessingCompleted";

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
        private Double confidenceScore;
        private Long processingTimeMs;
        private Integer pageCount;
        private Map<String, Object> extractedData;
    }
}
