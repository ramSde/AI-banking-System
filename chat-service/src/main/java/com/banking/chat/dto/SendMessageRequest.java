package com.banking.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Map;
import java.util.UUID;

@Schema(description = "Request to send a message in a chat session")
public record SendMessageRequest(
    
    @Schema(description = "Chat session ID", example = "123e4567-e89b-12d3-a456-426614174000", required = true)
    @NotNull(message = "Session ID is required")
    UUID sessionId,
    
    @Schema(description = "Message content", example = "What is my account balance?", required = true)
    @NotBlank(message = "Message content is required")
    @Size(max = 4000, message = "Message content must not exceed 4000 characters")
    String content,
    
    @Schema(description = "Whether to include RAG context", example = "true")
    Boolean includeRagContext,
    
    @Schema(description = "Whether to stream the response", example = "false")
    Boolean streamResponse,
    
    @Schema(description = "Additional metadata for the message")
    Map<String, Object> metadata
) {
    public SendMessageRequest {
        if (includeRagContext == null) {
            includeRagContext = true;
        }
        if (streamResponse == null) {
            streamResponse = false;
        }
    }
}
