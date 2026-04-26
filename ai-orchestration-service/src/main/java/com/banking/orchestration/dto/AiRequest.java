package com.banking.orchestration.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;

public record AiRequest(
        @NotBlank(message = "Prompt is required")
        @Size(max = 10000, message = "Prompt must not exceed 10000 characters")
        String prompt,

        String sessionId,

        @Size(max = 50, message = "Feature must not exceed 50 characters")
        String feature,

        String modelPreference,

        Integer maxTokens,

        Double temperature,

        List<String> context,

        Map<String, Object> metadata
) {
}
