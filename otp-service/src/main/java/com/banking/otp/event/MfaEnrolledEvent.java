package com.banking.otp.event;

import com.banking.otp.domain.MfaMethod;

import java.time.Instant;
import java.util.UUID;

/**
 * Event published when user enrolls in MFA
 */
public record MfaEnrolledEvent(
        String eventId,
        String eventType,
        String version,
        Instant occurredAt,
        String correlationId,
        Payload payload
) {
    public record Payload(
            UUID userId,
            MfaMethod mfaMethod,
            boolean verified,
            Instant enrolledAt
    ) {}

    public static MfaEnrolledEvent create(UUID userId, MfaMethod mfaMethod, boolean verified, String correlationId) {
        return new MfaEnrolledEvent(
                UUID.randomUUID().toString(),
                "MfaEnrolled",
                "1.0",
                Instant.now(),
                correlationId,
                new Payload(userId, mfaMethod, verified, Instant.now())
        );
    }
}
