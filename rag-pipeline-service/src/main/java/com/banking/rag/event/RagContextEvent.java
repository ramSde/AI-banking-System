package com.banking.rag.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RagContextEvent {

    private UUID eventId;
    private String eventType;
    @Builder.Default
    private String version = "1.0";
    @Builder.Default
    private Instant occurredAt = Instant.now();
    private String correlationId;
    private RagContextPayload payload;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RagContextPayload {
        private UUID contextId;
        private UUID queryId;
        private UUID userId;
        private Integer totalTokens;
        private Integer documentCount;
        private String traceId;
    }
}
