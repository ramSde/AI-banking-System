package com.banking.risk.event;

import com.banking.risk.domain.RiskAction;
import com.banking.risk.domain.RiskLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Event published when a risk assessment is completed.
 * Consumed by other services for audit and decision-making.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskAssessmentCompletedEvent {

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
        private RiskLevel riskLevel;
        private RiskAction riskAction;
        private Map<String, Integer> factors;
        private Boolean mfaRequired;
    }
}
