package com.banking.risk.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Event representing a login attempt from the Identity Service.
 * Consumed by the risk service to trigger risk assessment.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginAttemptedEvent {

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
        private UUID userId;
        private UUID sessionId;
        private String deviceFingerprint;
        private String ipAddress;
        private String userAgent;
        private Map<String, Object> geolocation;
    }
}
