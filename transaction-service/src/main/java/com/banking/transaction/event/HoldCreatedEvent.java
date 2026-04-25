package com.banking.transaction.event;

import com.banking.transaction.domain.HoldType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Hold Created Event
 * 
 * Published when a transaction hold is created.
 */
public record HoldCreatedEvent(
        String eventId,
        String eventType,
        String version,
        Instant occurredAt,
        String correlationId,
        Payload payload
) {
    public HoldCreatedEvent(UUID holdId, UUID accountId, HoldType holdType, BigDecimal amount,
                            String currency, Instant expiresAt, UUID createdBy) {
        this(
                UUID.randomUUID().toString(),
                "HoldCreated",
                "1.0",
                Instant.now(),
                holdId.toString(),
                new Payload(holdId, accountId, holdType, amount, currency, expiresAt, createdBy)
        );
    }

    public record Payload(
            UUID holdId,
            UUID accountId,
            HoldType holdType,
            BigDecimal amount,
            String currency,
            Instant expiresAt,
            UUID createdBy
    ) {}
}
