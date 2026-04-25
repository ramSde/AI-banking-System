package com.banking.notification.dto;

import com.banking.notification.domain.NotificationChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TemplateCreateRequest(
        @NotBlank(message = "Template code is required")
        @Size(max = 100, message = "Template code must not exceed 100 characters")
        String templateCode,

        @NotBlank(message = "Name is required")
        @Size(max = 200, message = "Name must not exceed 200 characters")
        String name,

        String description,

        @NotNull(message = "Channel is required")
        NotificationChannel channel,

        String subject,

        @NotBlank(message = "Body template is required")
        String bodyTemplate,

        Boolean active
) {
}
