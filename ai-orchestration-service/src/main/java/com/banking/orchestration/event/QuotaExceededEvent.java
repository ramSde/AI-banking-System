package com.banking.orchestration.event;

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
public class QuotaExceededEvent {

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
        private String quotaType;
        private Integer quotaLimit;
        private Integer currentUsage;
        private Integer attemptedTokens;
        private String userTier;
        private String traceId;
    }

    public static QuotaExceededEvent create(UUID userId, String quotaType, Integer quotaLimit,
                                            Integer currentUsage, Integer attemptedTokens,
                                            String userTier, String traceId) {
        return QuotaExceededEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("QuotaExceeded")
                .version("1.0")
                .occurredAt(Instant.now())
                .correlationId(traceId)
                .payload(Payload.builder()
                        .userId(userId)
                        .quotaType(quotaType)
                        .quotaLimit(quotaLimit)
                        .currentUsage(currentUsage)
                        .attemptedTokens(attemptedTokens)
                        .userTier(userTier)
                        .traceId(traceId)
                        .build())
                .build();
    }
}
