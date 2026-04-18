package com.banking.identity.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * API Response Wrapper
 * 
 * Standard response envelope for all API endpoints.
 * 
 * @param <T> Type of the response data
 */
@Schema(description = "Standard API response wrapper")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(

    @Schema(description = "Indicates if the request was successful", example = "true")
    boolean success,

    @Schema(description = "Response data (present on success)")
    T data,

    @Schema(description = "Error information (present on failure)")
    ErrorInfo error,

    @Schema(description = "Trace ID for request tracking", example = "123e4567-e89b-12d3-a456-426614174000")
    String traceId,

    @Schema(description = "Response timestamp in ISO 8601 format", example = "2024-01-01T00:00:00Z")
    Instant timestamp
) {
    /**
     * Create success response with data
     */
    public static <T> ApiResponse<T> success(T data, String traceId) {
        return new ApiResponse<>(true, data, null, traceId, Instant.now());
    }

    /**
     * Create success response without data
     */
    public static <T> ApiResponse<T> success(String traceId) {
        return new ApiResponse<>(true, null, null, traceId, Instant.now());
    }

    /**
     * Create error response
     */
    public static <T> ApiResponse<T> error(String code, String message, String traceId) {
        ErrorInfo errorInfo = new ErrorInfo(code, message, null);
        return new ApiResponse<>(false, null, errorInfo, traceId, Instant.now());
    }

    /**
     * Create error response with details
     */
    public static <T> ApiResponse<T> error(String code, String message, java.util.List<String> details, String traceId) {
        ErrorInfo errorInfo = new ErrorInfo(code, message, details);
        return new ApiResponse<>(false, null, errorInfo, traceId, Instant.now());
    }

    /**
     * Error Information
     */
    @Schema(description = "Error information")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ErrorInfo(
        @Schema(description = "Error code", example = "INVALID_CREDENTIALS")
        String code,

        @Schema(description = "Human-readable error message", example = "Invalid email or password")
        String message,

        @Schema(description = "Additional error details (field-level violations)")
        java.util.List<String> details
    ) {
    }
}
