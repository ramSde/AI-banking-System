package com.banking.notification.dto;

import com.banking.notification.domain.NotificationChannel;

import java.time.Instant;
import java.util.UUID;

public record TemplateResponse(
        UUID id,
        String templateCode,
        String name,
        String description,
        NotificationChannel channel,
        String subject,
        String bodyTemplate,
        Boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
