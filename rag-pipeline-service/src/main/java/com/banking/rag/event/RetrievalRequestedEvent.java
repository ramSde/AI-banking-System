package com.banking.rag.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetrievalRequestedEvent {

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
        private String queryText;
        private Integer topK;
        private BigDecimal similarityThreshold;
        private Boolean rerankingEnabled;
        private String sessionId;
        private String traceId;
    }
}
