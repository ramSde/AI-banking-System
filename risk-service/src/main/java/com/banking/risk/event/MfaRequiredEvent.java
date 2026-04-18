package com.banking.risk.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Event published when MFA is required due to elevated risk.
 * Triggers MFA flow in the OTP Service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MfaRequiredEvent {

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
        private UUID assessmentId;
        private UUID userId;
        private UUID sessionId;
        private Integer riskScore;
        private String reason;
    }
}
