package com.banking.chat.dto;

import com.banking.chat.domain.ChatSession;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Schema(description = "Chat session response")
public record SessionResponse(
    
    @Schema(description = "Session ID", example = "123e4567-e89b-12d3-a456-426614174000")
    UUID id,
    
    @Schema(description = "User ID", example = "123e4567-e89b-12d3-a456-426614174000")
    UUID userId,
    
    @Schema(description = "Session title", example = "Financial Planning Discussion")
    String title,
    
    @Schema(description = "Session status", example = "ACTIVE")
    ChatSession.SessionStatus status,
    
    @Schema(description = "Number of messages in the session", example = "15")
    Integer messageCount,
    
    @Schema(description = "Total tokens used in the session", example = "5000")
    Integer totalTokens,
    
    @Schema(description = "Additional metadata")
    Map<String, Object> metadata,
    
    @Schema(description = "Last activity timestamp")
    Instant lastActivityAt,
    
    @Schema(description = "Creation timestamp")
    Instant createdAt,
    
    @Schema(description = "Last update timestamp")
    Instant updatedAt
) {
}
