package com.banking.notification.dto;

import jakarta.validation.constraints.Size;

public record TemplateUpdateRequest(
        @Size(max = 200, message = "Name must not exceed 200 characters")
        String name,

        String description,

        String subject,

        String bodyTemplate,

        Boolean active
) {
}
