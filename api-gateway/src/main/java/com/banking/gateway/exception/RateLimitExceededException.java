package com.banking.gateway.exception;

/**
 * Rate Limit Exceeded Exception
 * 
 * Thrown when a client exceeds the configured rate limit.
 * 
 * Rate Limits:
 * - Per authenticated user: Configurable (default 100 requests/minute)
 * - Per IP address: Configurable (default 200 requests/minute)
 * 
 * HTTP Response:
 * - Status: 429 Too Many Requests
 * - Headers:
 *   - X-RateLimit-Limit: Maximum requests allowed
 *   - X-RateLimit-Remaining: 0
 *   - Retry-After: Seconds to wait before retrying
 * 
 * Client Handling:
 * - Clients should implement exponential backoff
 * - Respect Retry-After header value
 * - Consider implementing client-side rate limiting
 * 
 * @author Banking Platform Team
 * @version 1.0.0
 */
public class RateLimitExceededException extends RuntimeException {

    private final String limitType;
    private final int limit;
    private final int windowSeconds;

    public RateLimitExceededException(String limitType, int limit, int windowSeconds) {
        super(String.format("Rate limit exceeded for %s: %d requests per %d seconds", 
                limitType, limit, windowSeconds));
        this.limitType = limitType;
        this.limit = limit;
        this.windowSeconds = windowSeconds;
    }

    public String getLimitType() {
        return limitType;
    }

    public int getLimit() {
        return limit;
    }

    public int getWindowSeconds() {
        return windowSeconds;
    }
}
