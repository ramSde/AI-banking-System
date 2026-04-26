package com.banking.chat.dto;

import com.banking.chat.domain.ChatMessage;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Schema(description = "Chat message response")
public record MessageResponse(
    
    @Schema(description = "Message ID", example = "123e4567-e89b-12d3-a456-426614174000")
    UUID id,
    
    @Schema(description = "Session ID", example = "123e4567-e89b-12d3-a456-426614174000")
    UUID sessionId,
    
    @Schema(description = "Message role", example = "ASSISTANT")
    ChatMessage.MessageRole role,
    
    @Schema(description = "Message content", example = "Your current account balance is $5,432.10")
    String content,
    
    @Schema(description = "AI model name used", example = "gpt-4")
    String modelName,
    
    @Schema(description = "Input tokens used", example = "150")
    Integer inputTokens,
    
    @Schema(description = "Output tokens used", example = "75")
    Integer outputTokens,
    
    @Schema(description = "Total tokens used", example = "225")
    Integer totalTokens,
    
    @Schema(description = "Response latency in milliseconds", example = "1250")
    Long latencyMs,
    
    @Schema(description = "Source documents used for RAG")
    List<Map<String, Object>> sources,
    
    @Schema(description = "Additional metadata")
    Map<String, Object> metadata,
    
    @Schema(description = "Parent message ID for threaded conversations")
    UUID parentMessageId,
    
    @Schema(description = "Error message if any")
    String errorMessage,
    
    @Schema(description = "Whether this message contains an error", example = "false")
    Boolean isError,
    
    @Schema(description = "Creation timestamp")
    Instant createdAt,
    
    @Schema(description = "Last update timestamp")
    Instant updatedAt
) {
}
