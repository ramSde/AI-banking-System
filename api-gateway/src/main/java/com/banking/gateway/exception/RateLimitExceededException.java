package com.banking.gateway.exception;

/**
 * Exception thrown when rate limit is exceeded.
 * 
 * This exception is thrown when:
 * - IP-based rate limit is exceeded (too many requests from same IP)
 * - User-based rate limit is exceeded (authenticated user exceeds quota)
 * - Custom rate limits are violated (e.g., API endpoint specific limits)
 * 
 * Rate Limiting Context:
 * - Used in conjunction with Redis sliding window algorithm
 * - Supports both per-IP and per-user rate limiting
 * - Provides context for retry-after headers
 * - Enables proper HTTP 429 Too Many Requests responses
 * 
 * @author Banking Platform Team
 * @version 1.0.0
 */
public class RateLimitExceededException extends RuntimeException {

    private final String limitType;
    private final String identifier;
    private final int limit;
    private final int windowSeconds;

    /**
     * Create exception with basic error message.
     * 
     * @param message Error message describing the rate limit violation
     */
    public RateLimitExceededException(String message) {
        super(message);
        this.limitType = "unknown";
        this.identifier = "unknown";
        this.limit = 0;
        this.windowSeconds = 0;
    }

    /**
     * Create exception with detailed rate limit context.
     * 
     * @param message Error message describing the rate limit violation
     * @param limitType Type of rate limit (e.g., "IP", "USER", "ENDPOINT")
     * @param identifier The identifier that exceeded the limit (IP address, user ID, etc.)
     * @param limit The maximum number of requests allowed
     * @param windowSeconds The time window in seconds for the rate limit
     */
    public RateLimitExceededException(String message, String limitType, String identifier, 
                                    int limit, int windowSeconds) {
        super(message);
        this.limitType = limitType;
        this.identifier = identifier;
        this.limit = limit;
        this.windowSeconds = windowSeconds;
    }

    /**
     * Create exception with error message and root cause.
     * 
     * @param message Error message describing the rate limit violation
     * @param cause Root cause exception
     */
    public RateLimitExceededException(String message, Throwable cause) {
        super(message, cause);
        this.limitType = "unknown";
        this.identifier = "unknown";
        this.limit = 0;
        this.windowSeconds = 0;
    }

    /**
     * Get the type of rate limit that was exceeded.
     * 
     * @return Rate limit type (e.g., "IP", "USER", "ENDPOINT")
     */
    public String getLimitType() {
        return limitType;
    }

    /**
     * Get the identifier that exceeded the rate limit.
     * 
     * @return Identifier (IP address, user ID, etc.)
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Get the maximum number of requests allowed.
     * 
     * @return Request limit
     */
    public int getLimit() {
        return limit;
    }

    /**
     * Get the time window in seconds for the rate limit.
     * 
     * @return Window size in seconds
     */
    public int getWindowSeconds() {
        return windowSeconds;
    }

    /**
     * Calculate suggested retry-after time in seconds.
     * 
     * @return Suggested retry-after time
     */
    public int getRetryAfterSeconds() {
        // Suggest retrying after the window expires
        return Math.max(windowSeconds, 60); // Minimum 60 seconds
    }
}