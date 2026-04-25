package com.banking.transaction.event;

import com.banking.transaction.domain.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Transaction Created Event
 * 
 * Published when a new transaction is created (PENDING status).
 */
public record TransactionCreatedEvent(
        String eventId,
        String eventType,
        String version,
        Instant occurredAt,
        String correlationId,
        Payload payload
) {
    public TransactionCreatedEvent(UUID transactionId, String referenceNumber, TransactionType transactionType,
                                   UUID sourceAccountId, UUID destinationAccountId, BigDecimal amount,
                                   String currency, UUID initiatedBy, Map<String, Object> metadata) {
        this(
                UUID.randomUUID().toString(),
                "TransactionCreated",
                "1.0",
                Instant.now(),
                transactionId.toString(),
                new Payload(transactionId, referenceNumber, transactionType, sourceAccountId,
                        destinationAccountId, amount, currency, initiatedBy, metadata)
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
            UUID initiatedBy,
            Map<String, Object> metadata
    ) {}
}
