package com.banking.notification.event;

import com.banking.notification.domain.NotificationChannel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRequestedEvent {
    private UUID eventId;
    private String eventType;
    private String version;
    private Instant occurredAt;
    private UUID correlationId;
    private NotificationPayload payload;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NotificationPayload {
        private UUID userId;
        private String templateCode;
        private NotificationChannel channel;
        private String recipient;
        private Map<String, String> variables;
    }
}
