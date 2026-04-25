package com.banking.user.event;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

/**
 * Event published when a new user is created.
 * Topic: banking.user.user-created
 */
@Builder
public record UserCreatedEvent(
        String eventId,
        String eventType,
        String version,
        Instant occurredAt,
        String correlationId,
        UserCreatedPayload payload
) {
    public static UserCreatedEvent create(UUID userId, String email, String correlationId) {
        return UserCreatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("UserCreated")
                .version("1.0")
                .occurredAt(Instant.now())
                .correlationId(correlationId)
                .payload(new UserCreatedPayload(userId, email))
                .build();
    }

    public record UserCreatedPayload(
            UUID userId,
            String email
    ) {}
}
