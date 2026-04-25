package com.banking.fraud.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Fraud Alert Raised Event
 * 
 * Published when a fraud alert is raised.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FraudAlertRaisedEvent {

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
        private UUID alertId;
        private UUID fraudCheckId;
        private UUID transactionId;
        private UUID userId;
        private String alertType;
        private String severity;
        private Integer riskScore;
        private String description;
    }

    /**
     * Create fraud alert raised event
     * 
     * @param alertId Alert ID
     * @param fraudCheckId Fraud check ID
     * @param transactionId Transaction ID
     * @param userId User ID
     * @param alertType Alert type
     * @param severity Severity
     * @param riskScore Risk score
     * @param description Description
     * @return FraudAlertRaisedEvent
     */
    public static FraudAlertRaisedEvent create(
            UUID alertId,
            UUID fraudCheckId,
            UUID transactionId,
            UUID userId,
            String alertType,
            String severity,
            Integer riskScore,
            String description
    ) {
        Payload payload = Payload.builder()
                .alertId(alertId)
                .fraudCheckId(fraudCheckId)
                .transactionId(transactionId)
                .userId(userId)
                .alertType(alertType)
                .severity(severity)
                .riskScore(riskScore)
                .description(description)
                .build();

        return FraudAlertRaisedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("FraudAlertRaised")
                .version("1.0")
                .occurredAt(Instant.now())
                .correlationId(transactionId.toString())
                .payload(payload)
                .build();
    }
}
