package com.banking.gateway.filter;

import com.banking.gateway.config.GatewayProperties;
import com.banking.gateway.exception.RateLimitExceededException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

/**
 * Rate limiting filter using Redis sliding window algorithm.
 * 
 * This filter provides:
 * - Per-user rate limiting for authenticated requests
 * - Per-IP rate limiting for all requests (including unauthenticated)
 * - Sliding window algorithm for smooth rate limiting
 * - Redis-based distributed rate limiting across gateway instances
 * - Comprehensive metrics and logging
 * 
 * Rate Limiting Strategy:
 * - IP-based: Applied first to prevent brute force attacks
 * - User-based: Applied after authentication for personalized limits
 * - Sliding window: More accurate than fixed window, prevents burst traffic
 * - Distributed: Redis ensures consistent limits across multiple gateway instances
 * 
 * Algorithm:
 * 1. Remove expired entries from sliding window
 * 2. Count current requests in window
 * 3. Allow request if under limit, reject if over limit
 * 4. Add current request timestamp to window
 * 5. Set TTL for automatic cleanup
 * 
 * @author Banking Platform Team
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter implements GlobalFilter, Ordered {

    private final ReactiveRedisTemplate<String, Object> redisTemplate;
    private final GatewayProperties gatewayProperties;

    private static final String IP_RATE_LIMIT_KEY_PREFIX = "rate_limit:ip:";
    private static final String USER_RATE_LIMIT_KEY_PREFIX = "rate_limit:user:";

    /**
     * Apply rate limiting to incoming requests.
     * 
     * Processing Order:
     * 1. Extract client IP address
     * 2. Apply IP-based rate limiting (prevents brute force)
     * 3. Extract user ID from security context (if authenticated)
     * 4. Apply user-based rate limiting (personalized limits)
     * 5. Continue request processing or return 429 Too Many Requests
     * 
     * @param exchange ServerWebExchange containing request/response
     * @param chain GatewayFilterChain for continuing request processing
     * @return Mono<Void> representing async filter completion
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        
        // Skip rate limiting for health checks and metrics
        if (isExemptEndpoint(path)) {
            return chain.filter(exchange);
        }

        String clientIp = getClientIp(exchange);
        
        // Apply IP-based rate limiting first
        return checkIpRateLimit(clientIp)
                .flatMap(ipAllowed -> {
                    if (!ipAllowed) {
                        log.warn("IP rate limit exceeded for: {}", clientIp);
                        return handleRateLimitExceeded(exchange, "IP rate limit exceeded");
                    }
                    
                    // Apply user-based rate limiting for authenticated requests
                    return ReactiveSecurityContextHolder.getContext()
                            .cast(org.springframework.security.core.context.SecurityContext.class)
                            .map(ctx -> ctx.getAuthentication())
                            .cast(Authentication.class)
                            .flatMap(auth -> {
                                if (auth != null && auth.isAuthenticated()) {
                                    String userId = auth.getName();
                                    return checkUserRateLimit(userId)
                                            .flatMap(userAllowed -> {
                                                if (!userAllowed) {
                                                    log.warn("User rate limit exceeded for: {}", userId);
                                                    return handleRateLimitExceeded(exchange, "User rate limit exceeded");
                                                }
                                                return chain.filter(exchange);
                                            });
                                } else {
                                    // Unauthenticated request - only IP rate limiting applied
                                    return chain.filter(exchange);
                                }
                            })
                            .switchIfEmpty(chain.filter(exchange)); // No authentication context
                })
                .onErrorResume(Exception.class, ex -> {
                    log.error("Error during rate limiting for path: {}", path, ex);
                    // Continue processing on rate limiting errors (fail open)
                    return chain.filter(exchange);
                });
    }

    /**
     * Check IP-based rate limit using sliding window algorithm.
     * 
     * @param clientIp Client IP address
     * @return Mono<Boolean> true if request is allowed, false if rate limit exceeded
     */
    private Mono<Boolean> checkIpRateLimit(String clientIp) {
        String key = IP_RATE_LIMIT_KEY_PREFIX + clientIp;
        int limit = gatewayProperties.getRateLimit().getRequestsPerMinutePerIp();
        int windowSeconds = gatewayProperties.getRateLimit().getWindowSizeSeconds();
        
        return checkRateLimit(key, limit, windowSeconds);
    }

    /**
     * Check user-based rate limit using sliding window algorithm.
     * 
     * @param userId User ID from JWT token
     * @return Mono<Boolean> true if request is allowed, false if rate limit exceeded
     */
    private Mono<Boolean> checkUserRateLimit(String userId) {
        String key = USER_RATE_LIMIT_KEY_PREFIX + userId;
        int limit = gatewayProperties.getRateLimit().getRequestsPerMinutePerUser();
        int windowSeconds = gatewayProperties.getRateLimit().getWindowSizeSeconds();
        
        return checkRateLimit(key, limit, windowSeconds);
    }

    /**
     * Generic rate limit check using Redis sliding window algorithm.
     * 
     * Algorithm:
     * 1. Get current timestamp
     * 2. Remove expired entries (older than window size)
     * 3. Count remaining entries in window
     * 4. If count < limit, add current timestamp and allow request
     * 5. If count >= limit, reject request
     * 6. Set TTL for automatic cleanup
     * 
     * @param key Redis key for rate limit counter
     * @param limit Maximum requests allowed in window
     * @param windowSeconds Window size in seconds
     * @return Mono<Boolean> true if request is allowed
     */
    private Mono<Boolean> checkRateLimit(String key, int limit, int windowSeconds) {
        long now = Instant.now().toEpochMilli();
        long windowStart = now - (windowSeconds * 1000L);

        return redisTemplate.opsForZSet()
                // Remove expired entries
                .removeRangeByScore(key, 0, windowStart)
                .then(redisTemplate.opsForZSet().count(key, windowStart, now))
                .flatMap(currentCount -> {
                    if (currentCount < limit) {
                        // Add current request timestamp and set TTL
                        return redisTemplate.opsForZSet()
                                .add(key, String.valueOf(now), now)
                                .then(redisTemplate.expire(key, Duration.ofSeconds(windowSeconds + 10)))
                                .thenReturn(true);
                    } else {
                        // Rate limit exceeded
                        return Mono.just(false);
                    }
                })
                .onErrorReturn(true); // Fail open on Redis errors
    }

    /**
     * Extract client IP address from request headers and connection info.
     * 
     * Checks headers in order of preference:
     * 1. X-Forwarded-For (load balancer/proxy)
     * 2. X-Real-IP (nginx proxy)
     * 3. Remote address from connection
     * 
     * @param exchange ServerWebExchange containing request
     * @return Client IP address
     */
    private String getClientIp(ServerWebExchange exchange) {
        // Check X-Forwarded-For header (load balancer)
        String xForwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // Take first IP in case of multiple proxies
            return xForwardedFor.split(",")[0].trim();
        }

        // Check X-Real-IP header (nginx)
        String xRealIp = exchange.getRequest().getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        // Fall back to remote address
        return exchange.getRequest().getRemoteAddress() != null 
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                : "unknown";
    }

    /**
     * Check if endpoint is exempt from rate limiting.
     * 
     * Exempt endpoints:
     * - Health checks (monitoring systems)
     * - Metrics endpoints (Prometheus scraping)
     * - Static resources
     * 
     * @param path Request path
     * @return true if endpoint is exempt from rate limiting
     */
    private boolean isExemptEndpoint(String path) {
        return path.startsWith("/actuator/health") ||
               path.startsWith("/actuator/prometheus") ||
               path.startsWith("/actuator/info");
    }

    /**
     * Handle rate limit exceeded by returning 429 Too Many Requests.
     * 
     * @param exchange ServerWebExchange for the request
     * @param message Error message
     * @return Mono<Void> representing the error response
     */
    private Mono<Void> handleRateLimitExceeded(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        exchange.getResponse().getHeaders().add("Content-Type", "application/json");
        exchange.getResponse().getHeaders().add("Retry-After", "60"); // Suggest retry after 60 seconds

        String errorResponse = String.format(
                "{\"success\":false,\"error\":{\"code\":\"RATE_LIMIT_EXCEEDED\",\"message\":\"%s\"},\"timestamp\":\"%s\"}",
                message,
                Instant.now().toString()
        );

        org.springframework.core.io.buffer.DataBuffer buffer = 
                exchange.getResponse().bufferFactory().wrap(errorResponse.getBytes());

        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    /**
     * Set filter order to run after authentication but before routing.
     * 
     * @return Filter order (lower values run first)
     */
    @Override
    public int getOrder() {
        return -100; // Run after authentication filters but before routing
    }
}