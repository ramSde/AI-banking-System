package com.banking.stt.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Standard API response wrapper.
 * Provides consistent response structure across all endpoints.
 *
 * @param <T> Type of the response data
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2026-05-02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /**
     * Indicates if the request was successful
     */
    private boolean success;

    /**
     * Response data
     */
    private T data;

    /**
     * Error information (only present if success = false)
     */
    private ErrorInfo error;

    /**
     * Trace ID for request tracking
     */
    private String traceId;

    /**
     * Response timestamp
     */
    @Builder.Default
    private Instant timestamp = Instant.now();

    /**
     * Create a successful response.
     *
     * @param data Response data
     * @param <T>  Data type
     * @return ApiResponse instance
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Create a successful response with message.
     *
     * @param data    Response data
     * @param message Success message
     * @param <T>     Data type
     * @return ApiResponse instance
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Create an error response.
     *
     * @param code    Error code
     * @param message Error message
     * @param <T>     Data type
     * @return ApiResponse instance
     */
    public static <T> ApiResponse<T> error(String code, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(ErrorInfo.builder()
                        .code(code)
                        .message(message)
                        .build())
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Create an error response with details.
     *
     * @param code    Error code
     * @param message Error message
     * @param details Error details
     * @param <T>     Data type
     * @return ApiResponse instance
     */
    public static <T> ApiResponse<T> error(String code, String message, Object details) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(ErrorInfo.builder()
                        .code(code)
                        .message(message)
                        .details(details)
                        .build())
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Error information structure.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorInfo {
        private String code;
        private String message;
        private Object details;
    }
}
