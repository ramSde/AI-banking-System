package com.banking.chat.dto;

import com.banking.chat.domain.MessageFeedback;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Schema(description = "Message feedback response")
public record FeedbackResponse(
    
    @Schema(description = "Feedback ID", example = "123e4567-e89b-12d3-a456-426614174000")
    UUID id,
    
    @Schema(description = "Message ID", example = "123e4567-e89b-12d3-a456-426614174000")
    UUID messageId,
    
    @Schema(description = "User ID", example = "123e4567-e89b-12d3-a456-426614174000")
    UUID userId,
    
    @Schema(description = "Feedback rating", example = "POSITIVE")
    MessageFeedback.FeedbackRating rating,
    
    @Schema(description = "Feedback comment", example = "Very helpful response")
    String comment,
    
    @Schema(description = "Additional metadata")
    Map<String, Object> metadata,
    
    @Schema(description = "Creation timestamp")
    Instant createdAt,
    
    @Schema(description = "Last update timestamp")
    Instant updatedAt
) {
}
