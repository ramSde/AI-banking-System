package com.banking.chat.event;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record ChatSessionCreatedEvent(
    
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
    public ChatSessionCreatedEvent(UUID sessionId, UUID userId, String title, Map<String, Object> metadata) {
        this(
            UUID.randomUUID().toString(),
            "ChatSessionCreated",
            "1.0",
            Instant.now(),
            UUID.randomUUID().toString(),
            new Payload(sessionId, userId, title, metadata)
        );
    }

    public record Payload(
        @JsonProperty("session_id")
        UUID sessionId,
        
        @JsonProperty("user_id")
        UUID userId,
        
        @JsonProperty("title")
        String title,
        
        @JsonProperty("metadata")
        Map<String, Object> metadata
    ) {
    }
}
