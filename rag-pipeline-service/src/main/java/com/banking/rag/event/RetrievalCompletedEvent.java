package com.banking.rag.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetrievalCompletedEvent {

    private String eventId;
    
    private String eventType;
    
    @Builder.Default
    private String version = "1.0";
    
    @Builder.Default
    private Instant occurredAt = Instant.now();
    
    private String correlationId;
    
    private Payload payload;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Payload {
        private UUID queryId;
        private UUID userId;
        private UUID contextId;
        private Integer resultsCount;
        private Integer tokenCount;
        private Boolean cacheHit;
        private Long retrievalLatencyMs;
        private Long rerankingLatencyMs;
        private Long totalLatencyMs;
        private String traceId;
    }
}
