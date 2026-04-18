package com.banking.otp.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Event published when backup code is used
 */
public record BackupCodeUsedEvent(
        String eventId,
        String eventType,
        String version,
        Instant occurredAt,
        String correlationId,
        Payload payload
) {
    public record Payload(
            UUID userId,
            UUID backupCodeId,
            int remainingCodes,
            Instant usedAt
    ) {}

    public static BackupCodeUsedEvent create(UUID userId, UUID backupCodeId, int remainingCodes, String correlationId) {
        return new BackupCodeUsedEvent(
                UUID.randomUUID().toString(),
                "BackupCodeUsed",
                "1.0",
                Instant.now(),
                correlationId,
                new Payload(userId, backupCodeId, remainingCodes, Instant.now())
        );
    }
}
