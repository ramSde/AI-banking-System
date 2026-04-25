package com.banking.fraud.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Standard API Response Wrapper
 * 
 * @param <T> Response data type
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private T data;
    private ErrorDetails error;
    private String traceId;
    @Builder.Default
    private Instant timestamp = Instant.now();

    /**
     * Create success response
     * 
     * @param data Response data
     * @param <T> Data type
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .traceId(UUID.randomUUID().toString())
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Create error response
     * 
     * @param code Error code
     * @param message Error message
     * @param <T> Data type
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> error(String code, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(new ErrorDetails(code, message, null))
                .traceId(UUID.randomUUID().toString())
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Create error response with details
     * 
     * @param code Error code
     * @param message Error message
     * @param details Error details
     * @param <T> Data type
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> error(String code, String message, Object details) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(new ErrorDetails(code, message, details))
                .traceId(UUID.randomUUID().toString())
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Error Details
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorDetails {
        private String code;
        private String message;
        private Object details;
    }
}
