package com.banking.chat.event;

import com.banking.chat.domain.MessageFeedback;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public record MessageFeedbackSubmittedEvent(
    
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
    public MessageFeedbackSubmittedEvent(UUID feedbackId, UUID messageId, UUID userId, MessageFeedback.FeedbackRating rating, String comment) {
        this(
            UUID.randomUUID().toString(),
            "MessageFeedbackSubmitted",
            "1.0",
            Instant.now(),
            UUID.randomUUID().toString(),
            new Payload(feedbackId, messageId, userId, rating, comment)
        );
    }

    public record Payload(
        @JsonProperty("feedback_id")
        UUID feedbackId,
        
        @JsonProperty("message_id")
        UUID messageId,
        
        @JsonProperty("user_id")
        UUID userId,
        
        @JsonProperty("rating")
        MessageFeedback.FeedbackRating rating,
        
        @JsonProperty("comment")
        String comment
    ) {
    }
}
