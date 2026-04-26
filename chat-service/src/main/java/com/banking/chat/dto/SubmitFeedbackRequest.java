package com.banking.chat.dto;

import com.banking.chat.domain.MessageFeedback;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Map;
import java.util.UUID;

@Schema(description = "Request to submit feedback for a message")
public record SubmitFeedbackRequest(
    
    @Schema(description = "Message ID", example = "123e4567-e89b-12d3-a456-426614174000", required = true)
    @NotNull(message = "Message ID is required")
    UUID messageId,
    
    @Schema(description = "Feedback rating", example = "POSITIVE", required = true)
    @NotNull(message = "Rating is required")
    MessageFeedback.FeedbackRating rating,
    
    @Schema(description = "Optional feedback comment", example = "Very helpful response")
    @Size(max = 2000, message = "Comment must not exceed 2000 characters")
    String comment,
    
    @Schema(description = "Additional metadata for the feedback")
    Map<String, Object> metadata
) {
}
