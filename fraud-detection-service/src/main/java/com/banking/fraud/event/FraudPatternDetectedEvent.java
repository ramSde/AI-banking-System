package com.banking.fraud.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Fraud Pattern Detected Event
 * 
 * Published when a fraud pattern is detected.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FraudPatternDetectedEvent {

    private String eventId;
    private String eventType;
    private String version;
    private Instant occurredAt;
    private String correlationId;
    private Payload payload;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Payload {
        private UUID patternId;
        private String patternType;
        private String patternName;
        private UUID userId;
        private String severity;
        private Integer detectionCount;
        private Map<String, Object> patternData;
    }

    /**
     * Create fraud pattern detected event
     * 
     * @param patternId Pattern ID
     * @param patternType Pattern type
     * @param patternName Pattern name
     * @param userId User ID
     * @param severity Severity
     * @param detectionCount Detection count
     * @param patternData Pattern data
     * @return FraudPatternDetectedEvent
     */
    public static FraudPatternDetectedEvent create(
            UUID patternId,
            String patternType,
            String patternName,
            UUID userId,
            String severity,
            Integer detectionCount,
            Map<String, Object> patternData
    ) {
        Payload payload = Payload.builder()
                .patternId(patternId)
                .patternType(patternType)
                .patternName(patternName)
                .userId(userId)
                .severity(severity)
                .detectionCount(detectionCount)
                .patternData(patternData)
                .build();

        return FraudPatternDetectedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("FraudPatternDetected")
                .version("1.0")
                .occurredAt(Instant.now())
                .correlationId(userId != null ? userId.toString() : UUID.randomUUID().toString())
                .payload(payload)
                .build();
    }
}
