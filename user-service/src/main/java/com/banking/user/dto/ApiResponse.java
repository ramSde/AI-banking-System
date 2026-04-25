package com.banking.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.Instant;
import java.util.List;

/**
 * Standard API response wrapper for all endpoints.
 * Provides consistent response structure across the service.
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        T data,
        ErrorDetails error,
        String traceId,
        Instant timestamp
) {
    /**
     * Create success response
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Create success response with trace ID
     */
    public static <T> ApiResponse<T> success(T data, String traceId) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .traceId(traceId)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Create error response
     */
    public static <T> ApiResponse<T> error(String code, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(new ErrorDetails(code, message, null))
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Create error response with details
     */
    public static <T> ApiResponse<T> error(String code, String message, List<String> details) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(new ErrorDetails(code, message, details))
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Create error response with trace ID
     */
    public static <T> ApiResponse<T> error(String code, String message, String traceId) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(new ErrorDetails(code, message, null))
                .traceId(traceId)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Error details record
     */
    public record ErrorDetails(
            String code,
            String message,
            List<String> details
    ) {}
}
