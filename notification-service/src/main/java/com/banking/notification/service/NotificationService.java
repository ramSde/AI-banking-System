package com.banking.notification.service;

import com.banking.notification.dto.NotificationHistoryResponse;
import com.banking.notification.dto.NotificationQueryRequest;
import com.banking.notification.dto.NotificationSendRequest;
import com.banking.notification.dto.NotificationStatsResponse;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface NotificationService {
    void sendNotification(NotificationSendRequest request, UUID idempotencyKey, UUID traceId);
    Page<NotificationHistoryResponse> getNotificationHistory(NotificationQueryRequest request);
    NotificationHistoryResponse getNotificationById(UUID id);
    NotificationStatsResponse getNotificationStats(UUID userId);
}
