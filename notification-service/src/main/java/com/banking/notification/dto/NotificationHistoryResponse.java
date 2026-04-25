package com.banking.notification.dto;

import com.banking.notification.domain.NotificationChannel;
import com.banking.notification.domain.NotificationStatus;

import java.time.Instant;
import java.util.UUID;

public record NotificationHistoryResponse(
        UUID id,
        UUID userId,
        String templateCode,
        NotificationChannel channel,
        String recipient,
        String subject,
        NotificationStatus status,
        String errorMessage,
        Integer retryCount,
        UUID correlationId,
        UUID traceId,
        Instant sentAt,
        Instant createdAt
) {
}
