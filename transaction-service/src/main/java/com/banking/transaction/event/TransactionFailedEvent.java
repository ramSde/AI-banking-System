package com.banking.transaction.event;

import com.banking.transaction.domain.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Transaction Failed Event
 * 
 * Published when a transaction fails during processing.
 */
public record TransactionFailedEvent(
        String eventId,
        String eventType,
        String version,
        Instant occurredAt,
        String correlationId,
        Payload payload
) {
    public TransactionFailedEvent(UUID transactionId, String referenceNumber, TransactionType transactionType,
                                  BigDecimal amount, String currency, String failureReason, Instant failedAt) {
        this(
                UUID.randomUUID().toString(),
                "TransactionFailed",
                "1.0",
                Instant.now(),
                transactionId.toString(),
                new Payload(transactionId, referenceNumber, transactionType, amount, currency,
                        failureReason, failedAt)
        );
    }

    public record Payload(
            UUID transactionId,
            String referenceNumber,
            TransactionType transactionType,
            BigDecimal amount,
            String currency,
            String failureReason,
            Instant failedAt
    ) {}
}
