package com.banking.identity.event;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.UUID;

/**
 * User Registration Event
 * 
 * Published to Kafka when a new user registers.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserRegistrationEvent(
        String eventId,
        String eventType,
        String version,
        Instant occurredAt,
        String correlationId,
        UserRegistrationPayload payload
) {
    public UserRegistrationEvent(UserRegistrationPayload payload) {
        this(
                UUID.randomUUID().toString(),
                "UserRegistered",
                "1.0",
                Instant.now(),
                UUID.randomUUID().toString(),
                payload
        );
    }

    public record UserRegistrationPayload(
            String userId,
            String email,
            String phoneNumber,
            String username,
            Instant registeredAt
    ) {
    }
}
