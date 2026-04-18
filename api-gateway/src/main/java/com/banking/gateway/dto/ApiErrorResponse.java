package com.banking.gateway.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

/**
 * Standardized API error response format for the banking platform.
 * 
 * This DTO provides:
 * - Consistent error response structure across all services
 * - Machine-readable error codes for client handling
 * - Human-readable error messages for debugging
 * - Field-level validation errors for form handling
 * - Request tracing information for debugging
 * - Timestamp for error tracking and correlation
 * 
 * Response Format:
 * {
 *   "success": false,
 *   "data": null,
 *   "error": {
 *     "code": "VALIDATION_FAILED",
 *     "message": "Request validation failed",
 *     "details": ["Field 'email' is required", "Field 'amount' must be positive"]
 *   },
 *   "traceId": "550e8400-e29b-41d4-a716-446655440000",
 *   "timestamp": "2024-01-01T12:00:00Z"
 * }
 * 
 * @author Banking Platform Team
 * @version 1.0.0
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse {

    /**
     * Always false for error responses.
     */
    @Builder.Default
    private boolean success = false;

    /**
     * Always null for error responses (data field for consistency with success responses).
     */
    private Object data;

    /**
     * Error details containing code, message, and field-level details.
     */
    private ErrorDetails error;

    /**
     * Correlation ID for request tracing and debugging.
     */
    private String traceId;

    /**
     * Timestamp when the error occurred.
     */
    @Builder.Default
    private Instant timestamp = Instant.now();

    /**
     * Error details nested object.
     */
    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorDetails {

        /**
         * Machine-readable error code for client-side error handling.
         * 
         * Standard Error Codes:
         * - UNAUTHORIZED: Authentication required or failed
         * - FORBIDDEN: Access denied for authenticated user
         * - VALIDATION_FAILED: Request validation errors
         * - RATE_LIMIT_EXCEEDED: Too many requests
         * - SERVICE_UNAVAILABLE: Downstream service unavailable
         * - INTERNAL_ERROR: Unexpected server error
         * - INVALID_TOKEN: JWT token validation failed
         * - ACCOUNT_NOT_FOUND: Requested account does not exist
         * - INSUFFICIENT_FUNDS: Transaction amount exceeds available balance
         * - FRAUD_DETECTED: Transaction blocked by fraud detection
         */
        private String code;

        /**
         * Human-readable error message for debugging and logging.
         * Should not contain sensitive information.
         */
        private String message;

        /**
         * Field-level validation errors or additional error context.
         * Used for form validation errors and detailed error information.
         */
        private List<String> details;
    }

    /**
     * Create error response for authentication failures.
     * 
     * @param message Error message
     * @param traceId Request correlation ID
     * @return ApiErrorResponse for unauthorized access
     */
    public static ApiErrorResponse unauthorized(String message, String traceId) {
        return ApiErrorResponse.builder()
                .error(ErrorDetails.builder()
                        .code("UNAUTHORIZED")
                        .message(message)
                        .build())
                .traceId(traceId)
                .build();
    }

    /**
     * Create error response for authorization failures.
     * 
     * @param message Error message
     * @param traceId Request correlation ID
     * @return ApiErrorResponse for forbidden access
     */
    public static ApiErrorResponse forbidden(String message, String traceId) {
        return ApiErrorResponse.builder()
                .error(ErrorDetails.builder()
                        .code("FORBIDDEN")
                        .message(message)
                        .build())
                .traceId(traceId)
                .build();
    }

    /**
     * Create error response for validation failures.
     * 
     * @param message Error message
     * @param details List of field-level validation errors
     * @param traceId Request correlation ID
     * @return ApiErrorResponse for validation failures
     */
    public static ApiErrorResponse validationFailed(String message, List<String> details, String traceId) {
        return ApiErrorResponse.builder()
                .error(ErrorDetails.builder()
                        .code("VALIDATION_FAILED")
                        .message(message)
                        .details(details)
                        .build())
                .traceId(traceId)
                .build();
    }

    /**
     * Create error response for rate limit exceeded.
     * 
     * @param message Error message
     * @param traceId Request correlation ID
     * @return ApiErrorResponse for rate limit violations
     */
    public static ApiErrorResponse rateLimitExceeded(String message, String traceId) {
        return ApiErrorResponse.builder()
                .error(ErrorDetails.builder()
                        .code("RATE_LIMIT_EXCEEDED")
                        .message(message)
                        .build())
                .traceId(traceId)
                .build();
    }

    /**
     * Create error response for service unavailable.
     * 
     * @param message Error message
     * @param traceId Request correlation ID
     * @return ApiErrorResponse for service unavailability
     */
    public static ApiErrorResponse serviceUnavailable(String message, String traceId) {
        return ApiErrorResponse.builder()
                .error(ErrorDetails.builder()
                        .code("SERVICE_UNAVAILABLE")
                        .message(message)
                        .build())
                .traceId(traceId)
                .build();
    }

    /**
     * Create error response for internal server errors.
     * 
     * @param message Error message (should not contain sensitive information)
     * @param traceId Request correlation ID
     * @return ApiErrorResponse for internal errors
     */
    public static ApiErrorResponse internalError(String message, String traceId) {
        return ApiErrorResponse.builder()
                .error(ErrorDetails.builder()
                        .code("INTERNAL_ERROR")
                        .message(message)
                        .build())
                .traceId(traceId)
                .build();
    }

    /**
     * Create error response for invalid JWT tokens.
     * 
     * @param message Error message
     * @param traceId Request correlation ID
     * @return ApiErrorResponse for token validation failures
     */
    public static ApiErrorResponse invalidToken(String message, String traceId) {
        return ApiErrorResponse.builder()
                .error(ErrorDetails.builder()
                        .code("INVALID_TOKEN")
                        .message(message)
                        .build())
                .traceId(traceId)
                .build();
    }
}