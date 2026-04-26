package com.banking.chat.event;

import com.banking.chat.domain.ChatMessage;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public record ChatMessageSentEvent(
    
    @JsonProperty("event_id")
    String eventId,
    
    @JsonProperty("event_type")
    String eventType,
    
    @JsonProperty("version")
    String version,
    
    @JsonProperty("occurred_at")
    Instant occurredAt,
    
    @JsonProperty("correlation_id")
    String correlationId,
    
    @JsonProperty("payload")
    Payload payload
) {
    public ChatMessageSentEvent(UUID messageId, UUID sessionId, UUID userId, ChatMessage.MessageRole role, String content, Integer totalTokens) {
        this(
            UUID.randomUUID().toString(),
            "ChatMessageSent",
            "1.0",
            Instant.now(),
            UUID.randomUUID().toString(),
            new Payload(messageId, sessionId, userId, role, content, totalTokens)
        );
    }

    public record Payload(
        @JsonProperty("message_id")
        UUID messageId,
        
        @JsonProperty("session_id")
        UUID sessionId,
        
        @JsonProperty("user_id")
        UUID userId,
        
        @JsonProperty("role")
        ChatMessage.MessageRole role,
        
        @JsonProperty("content")
        String content,
        
        @JsonProperty("total_tokens")
        Integer totalTokens
    ) {
    }
}
