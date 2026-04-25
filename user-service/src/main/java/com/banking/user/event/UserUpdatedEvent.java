package com.banking.user.event;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

/**
 * Event published when a user profile is updated.
 * Topic: banking.user.user-updated
 */
@Builder
public record UserUpdatedEvent(
        String eventId,
        String eventType,
        String version,
        Instant occurredAt,
        String correlationId,
        UserUpdatedPayload payload
) {
    public static UserUpdatedEvent create(UUID userId, String email, String correlationId) {
        return UserUpdatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("UserUpdated")
                .version("1.0")
                .occurredAt(Instant.now())
                .correlationId(correlationId)
                .payload(new UserUpdatedPayload(userId, email))
                .build();
    }

    public record UserUpdatedPayload(
            UUID userId,
            String email
    ) {}
}
