package com.banking.user.event;

import com.banking.user.domain.KycStatus;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

/**
 * Event published when KYC status changes.
 * Topic: banking.user.kyc-status-changed
 */
@Builder
public record KycStatusChangedEvent(
        String eventId,
        String eventType,
        String version,
        Instant occurredAt,
        String correlationId,
        KycStatusChangedPayload payload
) {
    public static KycStatusChangedEvent create(UUID userId, KycStatus oldStatus, KycStatus newStatus, String correlationId) {
        return KycStatusChangedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("KycStatusChanged")
                .version("1.0")
                .occurredAt(Instant.now())
                .correlationId(correlationId)
                .payload(new KycStatusChangedPayload(userId, oldStatus, newStatus))
                .build();
    }

    public record KycStatusChangedPayload(
            UUID userId,
            KycStatus oldStatus,
            KycStatus newStatus
    ) {}
}
