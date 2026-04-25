package com.banking.transaction.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Hold Released Event
 * 
 * Published when a transaction hold is released.
 */
public record HoldReleasedEvent(
        String eventId,
        String eventType,
        String version,
        Instant occurredAt,
        String correlationId,
        Payload payload
) {
    public HoldReleasedEvent(UUID holdId, UUID accountId, BigDecimal amount, String currency,
                             boolean captured, UUID capturedTransactionId, Instant releasedAt) {
        this(
                UUID.randomUUID().toString(),
                "HoldReleased",
                "1.0",
                Instant.now(),
                holdId.toString(),
                new Payload(holdId, accountId, amount, currency, captured, capturedTransactionId, releasedAt)
        );
    }

    public record Payload(
            UUID holdId,
            UUID accountId,
            BigDecimal amount,
            String currency,
            boolean captured,
            UUID capturedTransactionId,
            Instant releasedAt
    ) {}
}
