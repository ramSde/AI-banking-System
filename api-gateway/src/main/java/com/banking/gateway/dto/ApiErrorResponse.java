package com.banking.gateway.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

/**
 * Standardized API error response format.
 * 
 * Provides consistent error response structure across all gateway endpoints:
 * - Uniform error format for client consumption
 * - Trace ID for request correlation and debugging
 * - Detailed error information with codes and messages
 * - Optional field-level validation errors
 * 
 * This format is used by all filters and error handlers to ensure
 * consistent error responses regardless of the failure type.
 * 
 * Example JSON response:
 * {
 *   "success": false,
 *   "data": null,
 *   "error": {
 *     "code": "AUTHENTICATION_FAILED",
 *     "message": "Invalid JWT token",
 *     "details": ["Token signature validation failed"]
 *   },
 *   "traceId": "550e8400-e29b-41d4-a716-446655440000",
 *   "timestamp": "2024-01-01T00:00:00Z"
 * }
 * 
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse {

    /**
     * Indicates whether the request was successful.
     * Always false for error responses.
     */
    private final boolean success;

    /**
     * Response data payload.
     * Always null for error responses.
     */
    private final Object data;

    /**
     * Error details containing code, message, and optional field details.
     */
    private final ErrorDetails error;

    /**
     * Unique trace ID for request correlation.
     * Used for debugging and log correlation across services.
     */
    private final String traceId;

    /**
     * Timestamp when the error occurred.
     */
    private final Instant timestamp;

    /**
     * Error details nested object.
     */
    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorDetails {

        /**
         * Machine-readable error code.
         * 
         * Standard error codes:
         * - AUTHENTICATION_FAILED: JWT validation failed
         * - RATE_LIMIT_EXCEEDED: Rate limit threshold breached
         * - INVALID_REQUEST: Malformed request
         * - SERVICE_UNAVAILABLE: Downstream service unavailable
         * - INTERNAL_ERROR: Unexpected server error
         */
        private final String code;

        /**
         * Human-readable error message.
         * Safe to display to end users.
         */
        private final String message;

        /**
         * Optional list of detailed error information.
         * Used for validation errors or additional context.
         */
        private final List<String> details;
    }
}