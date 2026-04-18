package com.banking.identity.event;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.UUID;

/**
 * Authentication Event
 * 
 * Published to Kafka when authentication events occur (login, logout, token refresh).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthenticationEvent(
        String eventId,
        String eventType,
        String version,
        Instant occurredAt,
        String correlationId,
        AuthenticationPayload payload
) {
    public AuthenticationEvent(String eventType, AuthenticationPayload payload) {
        this(
                UUID.randomUUID().toString(),
                eventType,
                "1.0",
                Instant.now(),
                UUID.randomUUID().toString(),
                payload
        );
    }

    public record AuthenticationPayload(
            String userId,
            String email,
            String eventAction,
            String ipAddress,
            String deviceId,
            String userAgent,
            boolean success,
            String failureReason
    ) {
    }
}
