package com.banking.notification.dto;

import com.banking.notification.domain.NotificationChannel;
import com.banking.notification.domain.NotificationStatus;

import java.time.Instant;
import java.util.UUID;

public record NotificationQueryRequest(
        UUID userId,
        NotificationChannel channel,
        NotificationStatus status,
        Instant startDate,
        Instant endDate,
        Integer page,
        Integer size
) {
}
