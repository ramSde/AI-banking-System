package com.banking.device.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

/**
 * Standard API response wrapper for all endpoints.
 * Provides consistent response structure across the service.
 */
@Schema(description = "Standard API response wrapper")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        
        @Schema(description = "Success status", example = "true")
        boolean success,
        
        @Schema(description = "Response data")
        T data,
        
        @Schema(description = "Error information")
        ErrorInfo error,
        
        @Schema(description = "Request trace ID", example = "123e4567-e89b-12d3-a456-426614174000")
        String traceId,
        
        @Schema(description = "Response timestamp", example = "2024-01-15T10:30:00Z")
        Instant timestamp
) {
    
    /**
     * Error information nested record
     */
    @Schema(description = "Error information")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ErrorInfo(
            @Schema(description = "Error code", example = "DEVICE_NOT_FOUND")
            String code,
            
            @Schema(description = "Human-readable error message", example = "Device not found")
            String message,
            
            @Schema(description = "Field-level validation errors")
            List<String> details
    ) {}
    
    /**
     * Creates a successful response
     */
    public static <T> ApiResponse<T> success(T data, String traceId) {
        return new ApiResponse<>(true, data, null, traceId, Instant.now());
    }
    
    /**
     * Creates a successful response without data
     */
    public static <T> ApiResponse<T> success(String traceId) {
        return new ApiResponse<>(true, null, null, traceId, Instant.now());
    }
    
    /**
     * Creates an error response
     */
    public static <T> ApiResponse<T> error(String code, String message, String traceId) {
        ErrorInfo error = new ErrorInfo(code, message, null);
        return new ApiResponse<>(false, null, error, traceId, Instant.now());
    }
    
    /**
     * Creates an error response with validation details
     */
    public static <T> ApiResponse<T> error(String code, String message, List<String> details, String traceId) {
        ErrorInfo error = new ErrorInfo(code, message, details);
        return new ApiResponse<>(false, null, error, traceId, Instant.now());
    }
}