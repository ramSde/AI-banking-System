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
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

/**
 * Rate Limiting Filter
 * 
 * Implements sliding window rate limiting using Redis.
 * Enforces per-user and per-IP request limits to prevent abuse.
 * 
 * Rate Limiting Strategy:
 * - Sliding window algorithm (more accurate than fixed window)
 * - Two independent limits: per authenticated user and per IP address
 * - Window size: Configurable (default 60 seconds)
 * - Limits: Configurable per user (default 100 req/min) and per IP (default 200 req/min)
 * 
 * Redis Key Structure:
 * - User limit: gateway:ratelimit:user:{userId}:{windowStart}
 * - IP limit: gateway:ratelimit:ip:{ipAddress}:{windowStart}
 * - TTL: 2x window size (to allow sliding window calculation)
 * 
 * Algorithm:
 * 1. Calculate current window start timestamp (rounded to window size)
 * 2. Increment counter for current window in Redis
 * 3. Get counter for previous window
 * 4. Calculate weighted count: current + (previous * overlap_percentage)
 * 5. Compare against limit
 * 6. Return 429 Too Many Requests if limit exceeded
 * 
 * Headers Added to Response:
 * - X-RateLimit-Limit: Maximum requests allowed
 * - X-RateLimit-Remaining: Requests remaining in current window
 * - X-RateLimit-Reset: Timestamp when limit resets
 * - Retry-After: Seconds to wait before retrying (on 429 response)
 * 
 * @author Banking Platform Team
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter implements GlobalFilter, Ordered {

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final GatewayProperties gatewayProperties;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!gatewayProperties.getRateLimit().isEnabled()) {
            return chain.filter(exchange);
        }

        String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
        String ipAddress = getClientIpAddress(exchange);

        Mono<Boolean> userLimitCheck = userId != null 
                ? checkRateLimit("user:" + userId, gatewayProperties.getRateLimit().getPerUserLimit())
                : Mono.just(true);

        Mono<Boolean> ipLimitCheck = checkRateLimit("ip:" + ipAddress, 
                gatewayProperties.getRateLimit().getPerIpLimit());

        return Mono.zip(userLimitCheck, ipLimitCheck)
                .flatMap(tuple -> {
                    boolean userAllowed = tuple.getT1();
                    boolean ipAllowed = tuple.getT2();

                    if (!userAllowed) {
                        log.warn("Rate limit exceeded for user: {}", userId);
                        return handleRateLimitExceeded(exchange, "user");
                    }

                    if (!ipAllowed) {
                        log.warn("Rate limit exceeded for IP: {}", ipAddress);
                        return handleRateLimitExceeded(exchange, "ip");
                    }

                    return chain.filter(exchange);
                });
    }

    private Mono<Boolean> checkRateLimit(String key, int limit) {
        long now = Instant.now().getEpochSecond();
        int windowSize = gatewayProperties.getRateLimit().getWindowSizeSeconds();
        long currentWindow = now / windowSize;
        long previousWindow = currentWindow - 1;

        String currentKey = "gateway:ratelimit:" + key + ":" + currentWindow;
        String previousKey = "gateway:ratelimit:" + key + ":" + previousWindow;

        return redisTemplate.opsForValue().increment(currentKey)
                .flatMap(currentCount -> {
                    redisTemplate.expire(currentKey, Duration.ofSeconds(windowSize * 2)).subscribe();

                    return redisTemplate.opsForValue().get(previousKey)
                            .defaultIfEmpty("0")
                            .map(prevCountStr -> {
                                long previousCount = Long.parseLong(prevCountStr);
                                double overlap = 1.0 - ((double) (now % windowSize) / windowSize);
                                double weightedCount = currentCount + (previousCount * overlap);

                                log.debug("Rate limit check for {}: current={}, previous={}, weighted={}, limit={}", 
                                        key, currentCount, previousCount, weightedCount, limit);

                                return weightedCount <= limit;
                            });
                });
    }

    private Mono<Void> handleRateLimitExceeded(ServerWebExchange exchange, String limitType) {
        int windowSize = gatewayProperties.getRateLimit().getWindowSizeSeconds();
        
        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        exchange.getResponse().getHeaders().add("X-RateLimit-Limit", 
                String.valueOf(limitType.equals("user") 
                        ? gatewayProperties.getRateLimit().getPerUserLimit()
                        : gatewayProperties.getRateLimit().getPerIpLimit()));
        exchange.getResponse().getHeaders().add("X-RateLimit-Remaining", "0");
        exchange.getResponse().getHeaders().add("Retry-After", String.valueOf(windowSize));
        
        return exchange.getResponse().setComplete();
    }

    private String getClientIpAddress(ServerWebExchange exchange) {
        String xForwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = exchange.getRequest().getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return exchange.getRequest().getRemoteAddress() != null
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                : "unknown";
    }

    @Override
    public int getOrder() {
        return -50;
    }
}
