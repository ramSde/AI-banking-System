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
public class RagCacheEvent {

    private UUID eventId;
    private String eventType;
    @Builder.Default
    private String version = "1.0";
    @Builder.Default
    private Instant occurredAt = Instant.now();
    private String correlationId;
    private RagCachePayload payload;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RagCachePayload {
        private UUID cacheId;
        private UUID userId;
        private String queryText;
        private Boolean hit;
        private Integer hitCount;
        private String traceId;
    }
}
