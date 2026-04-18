package com.banking.risk.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Standard API response wrapper for all endpoints.
 * Provides consistent response structure across the service.
 *
 * @param <T> Type of the response data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /**
     * Indicates if the request was successful
     */
    private Boolean success;

    /**
     * Response data (present on success)
     */
    private T data;

    /**
     * Error information (present on failure)
     */
    private ErrorDetails error;

    /**
     * Trace ID for request tracking
     */
    private String traceId;

    /**
     * Response timestamp
     */
    private Instant timestamp;

    /**
     * Create a successful response with data.
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
     * Create an error response.
     */
    public static <T> ApiResponse<T> error(ErrorDetails error, String traceId) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(error)
                .traceId(traceId)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Error details nested class.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ErrorDetails {
        private String code;
        private String message;
        private java.util.List<String> details;
    }
}
