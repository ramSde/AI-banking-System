package com.banking.orchestration.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiRequestStartedEvent {

    private String eventId;
    private String eventType;
    private String version;
    private Instant occurredAt;
    private String correlationId;
    private Payload payload;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Payload {
        private UUID userId;
        private String sessionId;
        private String feature;
        private String modelName;
        private String provider;
        private Integer estimatedTokens;
        private String traceId;
        private Map<String, Object> metadata;
    }

    public static AiRequestStartedEvent create(UUID userId, String sessionId, String feature, 
                                               String modelName, String provider, String traceId) {
        return AiRequestStartedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("AiRequestStarted")
                .version("1.0")
                .occurredAt(Instant.now())
                .correlationId(traceId)
                .payload(Payload.builder()
                        .userId(userId)
                        .sessionId(sessionId)
                        .feature(feature)
                        .modelName(modelName)
                        .provider(provider)
                        .traceId(traceId)
                        .build())
                .build();
    }
}
