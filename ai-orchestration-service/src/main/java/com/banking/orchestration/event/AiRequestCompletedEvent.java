package com.banking.orchestration.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiRequestCompletedEvent {

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
        private Integer inputTokens;
        private Integer outputTokens;
        private Integer totalTokens;
        private Long latencyMs;
        private BigDecimal costUsd;
        private String traceId;
        private Map<String, Object> metadata;
    }

    public static AiRequestCompletedEvent create(UUID userId, String sessionId, String feature,
                                                 String modelName, String provider, Integer inputTokens,
                                                 Integer outputTokens, Integer totalTokens, Long latencyMs,
                                                 BigDecimal costUsd, String traceId) {
        return AiRequestCompletedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("AiRequestCompleted")
                .version("1.0")
                .occurredAt(Instant.now())
                .correlationId(traceId)
                .payload(Payload.builder()
                        .userId(userId)
                        .sessionId(sessionId)
                        .feature(feature)
                        .modelName(modelName)
                        .provider(provider)
                        .inputTokens(inputTokens)
                        .outputTokens(outputTokens)
                        .totalTokens(totalTokens)
                        .latencyMs(latencyMs)
                        .costUsd(costUsd)
                        .traceId(traceId)
                        .build())
                .build();
    }
}
