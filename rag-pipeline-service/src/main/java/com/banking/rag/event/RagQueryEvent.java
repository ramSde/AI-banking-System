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
public class RagQueryEvent {

    private UUID eventId;
    private String eventType;
    @Builder.Default
    private String version = "1.0";
    @Builder.Default
    private Instant occurredAt = Instant.now();
    private String correlationId;
    private RagQueryPayload payload;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RagQueryPayload {
        private UUID queryId;
        private UUID userId;
        private UUID sessionId;
        private String queryText;
        private Integer topK;
        private Boolean rerankEnabled;
        private Integer maxContextTokens;
        private Boolean cacheHit;
        private Long totalLatencyMs;
        private String traceId;
    }
}
