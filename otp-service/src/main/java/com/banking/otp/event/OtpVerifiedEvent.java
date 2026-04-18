package com.banking.otp.event;

import com.banking.otp.domain.MfaMethod;

import java.time.Instant;
import java.util.UUID;

/**
 * Event published when OTP is successfully verified
 */
public record OtpVerifiedEvent(
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
            boolean success,
            Instant verifiedAt
    ) {}

    public static OtpVerifiedEvent create(UUID userId, MfaMethod mfaMethod, boolean success, String correlationId) {
        return new OtpVerifiedEvent(
                UUID.randomUUID().toString(),
                "OtpVerified",
                "1.0",
                Instant.now(),
                correlationId,
                new Payload(userId, mfaMethod, success, Instant.now())
        );
    }
}
