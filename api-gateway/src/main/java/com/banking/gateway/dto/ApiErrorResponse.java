package com.banking.gateway.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/**
 * API Error Response DTO
 * 
 * Standard error response format for all API Gateway errors.
 * Provides consistent error structure across all endpoints.
 * 
 * Response Structure:
 * {
 *   "success": false,
 *   "error": {
 *     "code": "RATE_LIMIT_EXCEEDED",
 *     "message": "Too many requests",
 *     "details": ["Limit: 100 requests per minute"]
 *   },
 *   "traceId": "uuid",
 *   "timestamp": "2024-01-01T00:00:00Z"
 * }
 * 
 * Error Codes:
 * - INVALID_TOKEN: JWT validation failed
 * - TOKEN_EXPIRED: JWT token has expired
 * - RATE_LIMIT_EXCEEDED: Rate limit exceeded
 * - SERVICE_UNAVAILABLE: Downstream service unavailable
 * - GATEWAY_TIMEOUT: Downstream service timeout
 * - INTERNAL_ERROR: Unexpected gateway error
 * 
 * @author Banking Platform Team
 * @version 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorResponse(
        boolean success,
        ErrorDetail error,
        String traceId,
        Instant timestamp
) {
    public ApiErrorResponse(String code, String message, String traceId) {
        this(false, new ErrorDetail(code, message, null), traceId, Instant.now());
    }

    public ApiErrorResponse(String code, String message, List<String> details, String traceId) {
        this(false, new ErrorDetail(code, message, details), traceId, Instant.now());
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ErrorDetail(
            String code,
            String message,
            List<String> details
    ) {}
}
