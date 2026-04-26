package com.banking.chat.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Standard API response wrapper")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
    
    @Schema(description = "Indicates if the request was successful", example = "true")
    boolean success,
    
    @Schema(description = "Response data")
    T data,
    
    @Schema(description = "Error information if request failed")
    ErrorInfo error,
    
    @Schema(description = "Trace ID for request tracking", example = "123e4567-e89b-12d3-a456-426614174000")
    String traceId,
    
    @Schema(description = "Response timestamp")
    Instant timestamp
) {
    public ApiResponse(T data) {
        this(true, data, null, UUID.randomUUID().toString(), Instant.now());
    }

    public ApiResponse(ErrorInfo error) {
        this(false, null, error, UUID.randomUUID().toString(), Instant.now());
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(data);
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(new ErrorInfo(code, message, null));
    }

    public static <T> ApiResponse<T> error(String code, String message, java.util.List<String> details) {
        return new ApiResponse<>(new ErrorInfo(code, message, details));
    }

    @Schema(description = "Error information")
    public record ErrorInfo(
        @Schema(description = "Error code", example = "SESSION_NOT_FOUND")
        String code,
        
        @Schema(description = "Error message", example = "Chat session not found")
        String message,
        
        @Schema(description = "Additional error details")
        java.util.List<String> details
    ) {
    }
}
