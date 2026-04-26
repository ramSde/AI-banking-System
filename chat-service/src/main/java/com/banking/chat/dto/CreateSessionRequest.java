package com.banking.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

import java.util.Map;

@Schema(description = "Request to create a new chat session")
public record CreateSessionRequest(
    
    @Schema(description = "Session title", example = "Financial Planning Discussion")
    @Size(max = 500, message = "Title must not exceed 500 characters")
    String title,
    
    @Schema(description = "Additional metadata for the session")
    Map<String, Object> metadata
) {
}
