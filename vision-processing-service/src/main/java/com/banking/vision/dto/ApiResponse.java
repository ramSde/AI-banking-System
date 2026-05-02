package com.banking.vision.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Standard API response envelope for all endpoints.
 * 
 * Provides consistent response structure with:
 * - Success indicator
 * - Data payload (generic type)
 * - Error details (when applicable)
 * - Trace ID for debugging
 * - Timestamp
 * 
 * @param <T> Type of data payload
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /**
     * Indicates if the request was successful.
     */
    private boolean success;

    /**
     * Response data payload (null on error).
     */
    private T data;

    /**
     * Error details (null on success).
     */
    private ErrorDetails error;

    /**
     * Trace ID for request tracking and debugging.
     */
    private String traceId;

    /**
     * Response timestamp (ISO 8601 UTC).
     */
    @Builder.Default
    private Instant timestamp = Instant.now();

    /**
     * Error details structure.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorDetails {
        /**
         * Error code (e.g., DOCUMENT_NOT_FOUND, INVALID_FILE_TYPE).
         */
        private String code;

        /**
         * Human-readable error message.
         */
        private String message;

        /**
         * Field-level validation errors (if applicable).
         */
        private List<String> details;
    }

    /**
     * Create success response with data.
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
     * Create error response.
     */
    public static <T> ApiResponse<T> error(String code, String message, String traceId) {
        return ApiResponse.<T>builder()
            .success(false)
            .error(ErrorDetails.builder()
                .code(code)
                .message(message)
                .build())
            .traceId(traceId)
            .timestamp(Instant.now())
            .build();
    }

    /**
     * Create error response with field details.
     */
    public static <T> ApiResponse<T> error(String code, String message, List<String> details, String traceId) {
        return ApiResponse.<T>builder()
            .success(false)
            .error(ErrorDetails.builder()
                .code(code)
                .message(message)
                .details(details)
                .build())
            .traceId(traceId)
            .timestamp(Instant.now())
            .build();
    }
}
