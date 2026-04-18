package com.banking.gateway.exception;

/**
 * Exception thrown when rate limit is exceeded.
 * 
 * This exception is thrown when:
 * - User exceeds per-user rate limit
 * - IP address exceeds per-IP rate limit
 * - Any other rate limiting threshold is breached
 * 
 * The exception includes information about the rate limit
 * that was exceeded and when the client can retry.
 * 
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2024-01-01
 */
public class RateLimitExceededException extends RuntimeException {

    private final int limit;
    private final long resetTimeMillis;
    private final String rateLimitType;

    /**
     * Constructs a new RateLimitExceededException with rate limit details.
     * 
     * @param message the detail message explaining the rate limit violation
     * @param limit the rate limit that was exceeded
     * @param resetTimeMillis timestamp when the rate limit window resets
     * @param rateLimitType type of rate limit (USER, IP, etc.)
     */
    public RateLimitExceededException(String message, int limit, long resetTimeMillis, String rateLimitType) {
        super(message);
        this.limit = limit;
        this.resetTimeMillis = resetTimeMillis;
        this.rateLimitType = rateLimitType;
    }

    /**
     * Constructs a new RateLimitExceededException with rate limit details and cause.
     * 
     * @param message the detail message explaining the rate limit violation
     * @param cause the underlying cause of the rate limit exception
     * @param limit the rate limit that was exceeded
     * @param resetTimeMillis timestamp when the rate limit window resets
     * @param rateLimitType type of rate limit (USER, IP, etc.)
     */
    public RateLimitExceededException(String message, Throwable cause, int limit, long resetTimeMillis, String rateLimitType) {
        super(message, cause);
        this.limit = limit;
        this.resetTimeMillis = resetTimeMillis;
        this.rateLimitType = rateLimitType;
    }

    /**
     * Gets the rate limit that was exceeded.
     * 
     * @return the rate limit value
     */
    public int getLimit() {
        return limit;
    }

    /**
     * Gets the timestamp when the rate limit window resets.
     * 
     * @return reset timestamp in milliseconds since epoch
     */
    public long getResetTimeMillis() {
        return resetTimeMillis;
    }

    /**
     * Gets the type of rate limit that was exceeded.
     * 
     * @return rate limit type (USER, IP, etc.)
     */
    public String getRateLimitType() {
        return rateLimitType;
    }
}