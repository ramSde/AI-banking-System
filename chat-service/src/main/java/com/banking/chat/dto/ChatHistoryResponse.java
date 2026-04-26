package com.banking.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

@Schema(description = "Chat history response with session and messages")
public record ChatHistoryResponse(
    
    @Schema(description = "Session information")
    SessionResponse session,
    
    @Schema(description = "List of messages in the session")
    List<MessageResponse> messages,
    
    @Schema(description = "Total number of messages", example = "15")
    Integer totalMessages,
    
    @Schema(description = "Whether there are more messages available", example = "false")
    Boolean hasMore
) {
}
