package com.banking.notification.dto;

import com.banking.notification.domain.NotificationChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

public record NotificationSendRequest(
        @NotNull(message = "User ID is required")
        UUID userId,

        @NotBlank(message = "Template code is required")
        String templateCode,

        @NotNull(message = "Channel is required")
        NotificationChannel channel,

        @NotBlank(message = "Recipient is required")
        String recipient,

        Map<String, String> variables,

        UUID correlationId
) {
}
