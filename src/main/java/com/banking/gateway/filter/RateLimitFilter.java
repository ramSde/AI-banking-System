package com.banking.gateway.filter;

import com.banking.gateway.config.GatewayProperties;
import com.banking.gateway.dto.ApiErrorResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

/**
 * Redis-backed sliding window rate limiter for the API Gateway.
 *
 * <p>Implements TWO independent rate limits per request:
 * <ol>
 *   <li><b>Per-user limit</b>: based on X-User-Id header (set by JwtAuthenticationFilter).
 *       Applies only to authenticated requests.</li>
 *   <li><b>Per-IP limit</b>: based on client IP address. Applies to ALL requests including
 *       unauthenticated ones. This prevents pre-auth brute force attacks.</li>
 * </ol>
 *
 * <p>Algorithm: Sliding window counter using Redis INCR + EXPIRE.
 * This approach is eventually consistent and may allow slight overcount
 * at window boundaries — acceptable for our use case. For exact limiting,
 * a Lua-script-based sliding log would be required (higher Redis overhead).
 *
 * <p>Key naming: {@code rl:{type}:{identifier}:{windowStart}}
 * Example: {@code rl:user:uuid123:1704067200}
 *
 * <p>Redis operations:
 * - INCR key (atomic increment, creates key if absent)
 * - EXPIRE key windowSeconds (set TTL if first request in window)
 *
 * <p>Ref: https://redis.io/docs/manual/patterns/rate-limiting/
 * Ref: https://docs.spring.io/spring-data/redis/docs/current/reference/html/
 */
@Slf4j
@Component
public class RateLimitFilter implements GlobalFilter, Ordered {

    /**
     * Lua script for atomic INCR + EXPIRE.
     * This ensures the TTL is set atomically with the first increment,
     * preventing keys from persisting forever if EXPIRE is never called.
     *
     * Returns the current count after increment.
     */
    private static final String RATE_LIMIT_LUA_SCRIPT = """
            local current = redis.call('INCR', KEYS[1])
            if current == 1 then
                redis.call('EXPIRE', KEYS[1], ARGV[1])
            end
            return current
            """;

    private static final String KEY_PREFIX = "rl:";
    private static final String HEADER_RATE_LIMIT_REMAINING = "X-RateLimit-Remaining";
    private static final String HEADER_RATE_LIMIT_RESET = "X-RateLimit-Reset";
    private static final String HEADER_RETRY_AFTER = "Retry-After";

    private final ReactiveStringRedisTemplate redisTemplate;
    private final GatewayProperties properties;
    private final ObjectMapper objectMapper;
    private final RedisScript<Long> rateLimitScript;

    public RateLimitFilter(
            final ReactiveStringRedisTemplate redisTemplate,
            final GatewayProperties properties,
            final ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.rateLimitScript = RedisScript.of(RATE_LIMIT_LUA_SCRIPT, Long.class);
    }

    /**
     * Runs after JWT authentication (order -200) so X-User-Id is available.
     * Runs before actual routing (order 0+).
     */
    @Override
    public int getOrder() {
        return -100;
    }

    @Override
    public Mono<Void> filter(
            final ServerWebExchange exchange,
            final GatewayFilterChain chain) {

        final ServerHttpRequest request = exchange.getRequest();
        final String traceId = request.getHeaders().getFirst(JwtAuthenticationFilter.HEADER_TRACE_ID);
        final String userId = request.getHeaders().getFirst(JwtAuthenticationFilter.HEADER_USER_ID);
        final String clientIp = extractClientIp(request);

        final GatewayProperties.RateLimit rl = properties.rateLimit();
        final long windowStart = System.currentTimeMillis() / 1000 / rl.windowSizeSeconds();
        final String windowTtl = String.valueOf(rl.windowSizeSeconds());

        // Per-IP rate limit (always checked)
        final String ipKey = KEY_PREFIX + "ip:" + clientIp + ":" + windowStart;

        return executeRateLimitScript(ipKey, windowTtl)
                .flatMap(ipCount -> {
                    if (ipCount > rl.requestsPerMinutePerIp()) {
                        log.warn("IP rate limit exceeded: ip={} count={} traceId={}", clientIp, ipCount, traceId);
                        return writeRateLimitResponse(exchange, rl.windowSizeSeconds(), traceId);
                    }

                    // Per-user rate limit (only for authenticated requests)
                    if (userId == null || userId.isBlank()) {
                        return chain.filter(addRateLimitHeaders(exchange, rl.requestsPerMinutePerIp() - ipCount));
                    }

                    final String userKey = KEY_PREFIX + "user:" + userId + ":" + windowStart;
                    return executeRateLimitScript(userKey, windowTtl)
                            .flatMap(userCount -> {
                                if (userCount > rl.requestsPerMinutePerUser()) {
                                    log.warn("User rate limit exceeded: userId={} count={} traceId={}", userId, userCount, traceId);
                                    return writeRateLimitResponse(exchange, rl.windowSizeSeconds(), traceId);
                                }

                                long remaining = Math.min(
                                        rl.requestsPerMinutePerUser() - userCount,
                                        rl.requestsPerMinutePerIp() - ipCount
                                );
                                return chain.filter(addRateLimitHeaders(exchange, remaining));
                            });
                })
                .onErrorResume(ex -> {
                    // If Redis is unavailable, allow the request through (fail-open).
                    // TRADE-OFF: This is a deliberate choice — denying all requests when
                    // Redis is down would cause a complete platform outage.
                    // Monitor Redis health and alert aggressively.
                    log.error("Redis unavailable for rate limiting — failing open: traceId={} error={}", traceId, ex.getMessage());
                    return chain.filter(exchange);
                });
    }

    /**
     * Executes the atomic INCR + EXPIRE Lua script against Redis.
     *
     * @param key       Redis key for this rate limit window
     * @param windowTtl TTL in seconds for the key
     * @return current request count after increment
     */
    private Mono<Long> executeRateLimitScript(final String key, final String windowTtl) {
        return redisTemplate.execute(
                rateLimitScript,
                List.of(key),
                List.of(windowTtl)
        ).next().defaultIfEmpty(1L);
    }

    /**
     * Adds standard rate limit response headers to inform clients of their quota.
     * Headers follow the IETF Rate Limiting Headers draft specification.
     *
     * @param exchange  current web exchange
     * @param remaining remaining requests in current window
     * @return exchange with headers added
     */
    private ServerWebExchange addRateLimitHeaders(
            final ServerWebExchange exchange,
            final long remaining) {
        exchange.getResponse().getHeaders()
                .set(HEADER_RATE_LIMIT_REMAINING, String.valueOf(Math.max(remaining, 0)));
        return exchange;
    }

    /**
     * Short-circuits the request with a 429 Too Many Requests response.
     *
     * @param exchange         current web exchange
     * @param retryAfterSeconds seconds until the rate limit window resets
     * @param traceId          correlation ID
     * @return completion signal
     */
    private Mono<Void> writeRateLimitResponse(
            final ServerWebExchange exchange,
            final long retryAfterSeconds,
            final String traceId) {

        final ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        response.getHeaders().set(HEADER_RETRY_AFTER, String.valueOf(retryAfterSeconds));
        response.getHeaders().set(HEADER_RATE_LIMIT_RESET, String.valueOf(retryAfterSeconds));

        final ApiErrorResponse errorResponse = ApiErrorResponse.of(
                "RATE_LIMIT_EXCEEDED",
                "Too many requests. Please retry after " + retryAfterSeconds + " seconds.",
                traceId
        );

        try {
            final byte[] bytes = objectMapper.writeValueAsBytes(errorResponse);
            final DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException ex) {
            log.error("Failed to serialize rate limit response", ex);
            final byte[] fallback = "Too many requests".getBytes(StandardCharsets.UTF_8);
            final DataBuffer buffer = response.bufferFactory().wrap(fallback);
            return response.writeWith(Mono.just(buffer));
        }
    }

    /**
     * Extracts the real client IP address, respecting common proxy headers.
     *
     * <p>Priority:
     * <ol>
     *   <li>X-Forwarded-For (first entry — the original client IP)</li>
     *   <li>X-Real-IP</li>
     *   <li>Remote address from TCP connection</li>
     * </ol>
     *
     * <p>SECURITY NOTE: X-Forwarded-For can be spoofed by clients if not validated
     * at the load balancer. In production Kubernetes, ensure the ingress controller
     * (NGINX/Istio) overwrites this header — do not trust arbitrary client values.
     *
     * @param request incoming HTTP request
     * @return client IP string (never null — falls back to "unknown")
     */
    private String extractClientIp(final ServerHttpRequest request) {
        final String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }

        final String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank()) {
            return xRealIp.trim();
        }

        InetAddress remoteAddress = request.getRemoteAddress() != null
                ? request.getRemoteAddress().getAddress()
                : null;
        return remoteAddress != null ? remoteAddress.getHostAddress() : "unknown";
    }
}
