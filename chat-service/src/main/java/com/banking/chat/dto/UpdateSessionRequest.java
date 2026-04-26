package com.banking.chat.dto;

import com.banking.chat.domain.ChatSession;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

import java.util.Map;

@Schema(description = "Request to update a chat session")
public record UpdateSessionRequest(
    
    @Schema(description = "Session title", example = "Updated Financial Planning Discussion")
    @Size(max = 500, message = "Title must not exceed 500 characters")
    String title,
    
    @Schema(description = "Session status", example = "ACTIVE")
    ChatSession.SessionStatus status,
    
    @Schema(description = "Additional metadata for the session")
    Map<String, Object> metadata
) {
}
