package com.banking.notification.dto;

import java.util.Map;

public record NotificationStatsResponse(
        Long totalSent,
        Long totalFailed,
        Long totalPending,
        Map<String, Long> byChannel,
        Map<String, Long> byStatus
) {
}
