package com.banking.transaction.event;

import com.banking.transaction.domain.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Transaction Completed Event
 * 
 * Published when a transaction is successfully completed.
 */
public record TransactionCompletedEvent(
        String eventId,
        String eventType,
        String version,
        Instant occurredAt,
        String correlationId,
        Payload payload
) {
    public TransactionCompletedEvent(UUID transactionId, String referenceNumber, TransactionType transactionType,
                                     UUID sourceAccountId, UUID destinationAccountId, BigDecimal amount,
                                     String currency, Instant completedAt) {
        this(
                UUID.randomUUID().toString(),
                "TransactionCompleted",
                "1.0",
                Instant.now(),
                transactionId.toString(),
                new Payload(transactionId, referenceNumber, transactionType, sourceAccountId,
                        destinationAccountId, amount, currency, completedAt)
        );
    }

    public record Payload(
            UUID transactionId,
            String referenceNumber,
            TransactionType transactionType,
            UUID sourceAccountId,
            UUID destinationAccountId,
            BigDecimal amount,
            String currency,
            Instant completedAt
    ) {}
}
