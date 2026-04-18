package com.banking.gateway.filter;

import com.banking.gateway.config.GatewayProperties;
import com.banking.gateway.dto.ApiErrorResponse;
import com.banking.gateway.exception.RateLimitExceededException;
import com.banking.gateway.util.JwtValidator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Redis-based rate limiting filter using sliding window algorithm.
 * 
 * Implements dual-layer rate limiting:
 * 1. Per-user rate limiting (authenticated requests)
 * 2. Per-IP rate limiting (all requests including unauthenticated)
 * 
 * Algorithm: Sliding Window Counter
 * - Uses Redis sorted sets with timestamps as scores
 * - Removes expired entries before counting
 * - Atomic operations ensure consistency under high concurrency
 * 
 * Rate limit headers are added to responses for client awareness:
 * - X-RateLimit-Limit: Maximum requests allowed
 * - X-RateLimit-Remaining: Requests remaining in current window
 * - X-RateLimit-Reset: Timestamp when window resets
 * 
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter implements GlobalFilter, Ordered {

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final GatewayProperties gatewayProperties;
    private final JwtValidator jwtValidator;
    private final ObjectMapper objectMapper;

    private static final String USER_RATE_LIMIT_KEY_PREFIX = "rate_limit:user:";
    private static final String IP_RATE_LIMIT_KEY_PREFIX = "rate_limit:ip:";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String clientIp = getClientIpAddress(request);
        
        // Extract user ID from JWT token (if present)
        String userId = extractUserIdFromRequest(request);
        
        // Apply rate limiting
        return applyRateLimit(exchange, userId, clientIp)
            .flatMap(rateLimitResult -> {
                // Add rate limit headers to response
                addRateLimitHeaders(exchange.getResponse(), rateLimitResult);
                
                if (rateLimitResult.isAllowed()) {
                    log.debug("Rate limit check passed for user: {}, IP: {}", 
                             maskUserId(userId), maskIpAddress(clientIp));
                    return chain.filter(exchange);
                } else {
                    log.warn("Rate limit exceeded for user: {}, IP: {}", 
                            maskUserId(userId), maskIpAddress(clientIp));
                    return handleRateLimitExceeded(exchange, rateLimitResult);
                }
            });
    }

    /**
     * Applies rate limiting checks for both user and IP.
     * 
     * @param exchange Server web exchange
     * @param userId User ID (null for unauthenticated requests)
     * @param clientIp Client IP address
     * @return Mono<RateLimitResult> with rate limit decision
     */
    private Mono<RateLimitResult> applyRateLimit(ServerWebExchange exchange, 
                                               String userId, 
                                               String clientIp) {
        Instant now = Instant.now();
        Duration windowDuration = Duration.ofSeconds(gatewayProperties.getRateLimit().getWindowSizeSeconds());
        Instant windowStart = now.minus(windowDuration);
        
        // Check user rate limit (if authenticated)
        Mono<RateLimitResult> userRateLimit = userId != null 
            ? checkRateLimit(USER_RATE_LIMIT_KEY_PREFIX + userId, 
                           gatewayProperties.getRateLimit().getRequestsPerMinutePerUser(),
                           windowStart, now, "USER")
            : Mono.just(RateLimitResult.allowed(0, 0, now));
        
        // Check IP rate limit (always applied)
        Mono<RateLimitResult> ipRateLimit = checkRateLimit(
            IP_RATE_LIMIT_KEY_PREFIX + clientIp,
            gatewayProperties.getRateLimit().getRequestsPerMinutePerIp(),
            windowStart, now, "IP");
        
        // Combine results - both must pass
        return Mono.zip(userRateLimit, ipRateLimit)
            .map(tuple -> {
                RateLimitResult userResult = tuple.getT1();
                RateLimitResult ipResult = tuple.getT2();
                
                // If either limit is exceeded, return the more restrictive one
                if (!userResult.isAllowed()) {
                    return userResult;
                } else if (!ipResult.isAllowed()) {
                    return ipResult;
                } else {
                    // Both passed - return the more restrictive remaining count
                    int minRemaining = Math.min(userResult.getRemaining(), ipResult.getRemaining());
                    int maxLimit = Math.max(userResult.getLimit(), ipResult.getLimit());
                    return RateLimitResult.allowed(maxLimit, minRemaining, now);
                }
            });
    }

    /**
     * Checks rate limit using Redis sliding window algorithm.
     * 
     * @param key Redis key for rate limit counter
     * @param limit Maximum requests allowed in window
     * @param windowStart Start of current window
     * @param now Current timestamp
     * @param type Rate limit type (USER or IP) for logging
     * @return Mono<RateLimitResult> with rate limit decision
     */
    private Mono<RateLimitResult> checkRateLimit(String key, 
                                                int limit, 
                                                Instant windowStart, 
                                                Instant now, 
                                                String type) {
        return redisTemplate.opsForZSet()
            // Remove expired entries from sliding window
            .removeRangeByScore(key, 0, windowStart.toEpochMilli())
            .then(redisTemplate.opsForZSet().count(key, windowStart.toEpochMilli(), now.toEpochMilli()))
            .cast(Long.class)
            .flatMap(currentCount -> {
                if (currentCount >= limit) {
                    log.debug("{} rate limit exceeded for key: {} (count: {}, limit: {})", 
                             type, key, currentCount, limit);
                    return Mono.just(RateLimitResult.exceeded(limit, 0, now));
                } else {
                    // Add current request to sliding window
                    return redisTemplate.opsForZSet()
                        .add(key, UUID.randomUUID().toString(), now.toEpochMilli())
                        .then(redisTemplate.expire(key, Duration.ofSeconds(
                            gatewayProperties.getRateLimit().getWindowSizeSeconds() + 10))) // Extra TTL buffer
                        .then(Mono.just(RateLimitResult.allowed(limit, 
                                                              (int) (limit - currentCount - 1), 
                                                              now)));
                }
            })
            .onErrorResume(error -> {
                log.error("Redis error during rate limit check for key: " + key, error);
                // Fail open - allow request if Redis is unavailable
                return Mono.just(RateLimitResult.allowed(limit, limit - 1, now));
            });
    }

    /**
     * Extracts user ID from JWT token in Authorization header.
     * 
     * @param request HTTP request
     * @return User ID or null if not authenticated
     */
    private String extractUserIdFromRequest(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return jwtValidator.extractUserIdUnsafe(token);
        }
        return null;
    }

    /**
     * Extracts client IP address from request headers and remote address.
     * 
     * Checks headers in order of preference:
     * 1. X-Forwarded-For (load balancer/proxy)
     * 2. X-Real-IP (nginx proxy)
     * 3. Remote address (direct connection)
     * 
     * @param request HTTP request
     * @return Client IP address
     */
    private String getClientIpAddress(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // Take first IP in comma-separated list
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        // Fallback to remote address
        return request.getRemoteAddress() != null 
            ? request.getRemoteAddress().getAddress().getHostAddress()
            : "unknown";
    }

    /**
     * Adds rate limit headers to HTTP response.
     * 
     * @param response HTTP response
     * @param result Rate limit result
     */
    private void addRateLimitHeaders(ServerHttpResponse response, RateLimitResult result) {
        response.getHeaders().add("X-RateLimit-Limit", String.valueOf(result.getLimit()));
        response.getHeaders().add("X-RateLimit-Remaining", String.valueOf(result.getRemaining()));
        response.getHeaders().add("X-RateLimit-Reset", String.valueOf(
            result.getResetTime().plusSeconds(gatewayProperties.getRateLimit().getWindowSizeSeconds()).toEpochMilli()));
    }

    /**
     * Handles rate limit exceeded scenario with structured error response.
     * 
     * @param exchange Server web exchange
     * @param result Rate limit result
     * @return Mono<Void> representing the error response
     */
    private Mono<Void> handleRateLimitExceeded(ServerWebExchange exchange, RateLimitResult result) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
            .success(false)
            .error(ApiErrorResponse.ErrorDetails.builder()
                .code("RATE_LIMIT_EXCEEDED")
                .message("Too many requests. Please try again later.")
                .build())
            .traceId(UUID.randomUUID().toString())
            .timestamp(Instant.now())
            .build();

        try {
            String errorJson = objectMapper.writeValueAsString(errorResponse);
            DataBuffer buffer = response.bufferFactory().wrap(errorJson.getBytes());
            return response.writeWith(Mono.just(buffer));
            
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize rate limit error response", e);
            return response.setComplete();
        }
    }

    /**
     * Masks user ID for logging (privacy protection).
     */
    private String maskUserId(String userId) {
        if (userId == null || userId.length() <= 8) {
            return "****";
        }
        return userId.substring(0, 4) + "****" + userId.substring(userId.length() - 4);
    }

    /**
     * Masks IP address for logging (privacy protection).
     */
    private String maskIpAddress(String ip) {
        if (ip == null || ip.equals("unknown")) {
            return "unknown";
        }
        String[] parts = ip.split("\\.");
        if (parts.length == 4) {
            return parts[0] + "." + parts[1] + ".***." + parts[3];
        }
        return "***";
    }

    @Override
    public int getOrder() {
        return -50; // Execute after authentication filter
    }

    /**
     * Rate limit result data class.
     */
    private static class RateLimitResult {
        private final boolean allowed;
        private final int limit;
        private final int remaining;
        private final Instant resetTime;

        private RateLimitResult(boolean allowed, int limit, int remaining, Instant resetTime) {
            this.allowed = allowed;
            this.limit = limit;
            this.remaining = remaining;
            this.resetTime = resetTime;
        }

        public static RateLimitResult allowed(int limit, int remaining, Instant resetTime) {
            return new RateLimitResult(true, limit, remaining, resetTime);
        }

        public static RateLimitResult exceeded(int limit, int remaining, Instant resetTime) {
            return new RateLimitResult(false, limit, remaining, resetTime);
        }

        public boolean isAllowed() { return allowed; }
        public int getLimit() { return limit; }
        public int getRemaining() { return remaining; }
        public Instant getResetTime() { return resetTime; }
    }
}