package com.banking.otp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * Standard API response wrapper
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard API response wrapper")
public record ApiResponse<T>(
        @Schema(description = "Indicates if the request was successful", example = "true")
        boolean success,

        @Schema(description = "Response data")
        T data,

        @Schema(description = "Error information (present only if success is false)")
        ErrorInfo error,

        @Schema(description = "Trace ID for request tracking", example = "550e8400-e29b-41d4-a716-446655440000")
        String traceId,

        @Schema(description = "Response timestamp", example = "2024-01-01T00:00:00Z")
        Instant timestamp
) {
    public static <T> ApiResponse<T> success(T data, String traceId) {
        return new ApiResponse<>(true, data, null, traceId, Instant.now());
    }

    public static <T> ApiResponse<T> error(String code, String message, String traceId) {
        return new ApiResponse<>(false, null, new ErrorInfo(code, message, null), traceId, Instant.now());
    }

    public static <T> ApiResponse<T> error(String code, String message, java.util.List<String> details, String traceId) {
        return new ApiResponse<>(false, null, new ErrorInfo(code, message, details), traceId, Instant.now());
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ErrorInfo(
            @Schema(description = "Error code", example = "INVALID_OTP")
            String code,

            @Schema(description = "Human-readable error message", example = "The provided OTP is invalid or has expired")
            String message,

            @Schema(description = "Additional error details")
            java.util.List<String> details
    ) {}
}
