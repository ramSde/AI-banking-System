package com.banking.fraud.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Transaction Blocked Event
 * 
 * Published when a transaction is blocked due to fraud.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionBlockedEvent {

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
        private UUID transactionId;
        private UUID userId;
        private UUID fraudCheckId;
        private Integer riskScore;
        private String riskLevel;
        private List<String> triggeredRules;
        private String reason;
    }

    /**
     * Create transaction blocked event
     * 
     * @param transactionId Transaction ID
     * @param userId User ID
     * @param fraudCheckId Fraud check ID
     * @param riskScore Risk score
     * @param riskLevel Risk level
     * @param triggeredRules Triggered rules
     * @param reason Block reason
     * @return TransactionBlockedEvent
     */
    public static TransactionBlockedEvent create(
            UUID transactionId,
            UUID userId,
            UUID fraudCheckId,
            Integer riskScore,
            String riskLevel,
            List<String> triggeredRules,
            String reason
    ) {
        Payload payload = Payload.builder()
                .transactionId(transactionId)
                .userId(userId)
                .fraudCheckId(fraudCheckId)
                .riskScore(riskScore)
                .riskLevel(riskLevel)
                .triggeredRules(triggeredRules)
                .reason(reason)
                .build();

        return TransactionBlockedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("TransactionBlocked")
                .version("1.0")
                .occurredAt(Instant.now())
                .correlationId(transactionId.toString())
                .payload(payload)
                .build();
    }
}
