package com.banking.transaction.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Transaction Reversed Event
 * 
 * Published when a transaction is reversed.
 */
public record TransactionReversedEvent(
        String eventId,
        String eventType,
        String version,
        Instant occurredAt,
        String correlationId,
        Payload payload
) {
    public TransactionReversedEvent(UUID originalTransactionId, String originalReference,
                                    UUID reversalTransactionId, String reversalReference,
                                    BigDecimal amount, String currency, String reason, Instant reversedAt) {
        this(
                UUID.randomUUID().toString(),
                "TransactionReversed",
                "1.0",
                Instant.now(),
                originalTransactionId.toString(),
                new Payload(originalTransactionId, originalReference, reversalTransactionId,
                        reversalReference, amount, currency, reason, reversedAt)
        );
    }

    public record Payload(
            UUID originalTransactionId,
            String originalReference,
            UUID reversalTransactionId,
            String reversalReference,
            BigDecimal amount,
            String currency,
            String reason,
            Instant reversedAt
    ) {}
}
