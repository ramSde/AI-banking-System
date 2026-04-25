package com.banking.transaction.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.UUID;

/**
 * Standard API Response Wrapper
 * 
 * Wraps all API responses with consistent structure including success flag,
 * data payload, error details, trace ID, and timestamp.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        T data,
        ErrorDetails error,
        String traceId,
        Instant timestamp
) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(
                true,
                data,
                null,
                UUID.randomUUID().toString(),
                Instant.now()
        );
    }

    public static <T> ApiResponse<T> error(String code, String message, java.util.List<String> details) {
        return new ApiResponse<>(
                false,
                null,
                new ErrorDetails(code, message, details),
                UUID.randomUUID().toString(),
                Instant.now()
        );
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return error(code, message, null);
    }

    public record ErrorDetails(
            String code,
            String message,
            java.util.List<String> details
    ) {}
}
