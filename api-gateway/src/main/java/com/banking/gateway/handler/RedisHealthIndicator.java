package com.banking.gateway.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuator.health.Health;
import org.springframework.boot.actuator.health.ReactiveHealthIndicator;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Custom health indicator for Redis connectivity.
 * 
 * Provides detailed health information about Redis connection status:
 * - Connection availability
 * - Response time measurement
 * - Error details for troubleshooting
 * 
 * This health check is used by:
 * - Kubernetes readiness probes
 * - Load balancer health checks
 * - Monitoring systems (Prometheus, Grafana)
 * - Operations teams for troubleshooting
 * 
 * The health check performs a simple PING operation to verify
 * Redis connectivity without impacting performance.
 * 
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisHealthIndicator implements ReactiveHealthIndicator {

    private final ReactiveRedisTemplate<String, String> redisTemplate;

    private static final Duration HEALTH_CHECK_TIMEOUT = Duration.ofSeconds(2);
    private static final String HEALTH_CHECK_KEY = "health:check";

    @Override
    public Mono<Health> health() {
        long startTime = System.currentTimeMillis();
        
        return redisTemplate.opsForValue()
            .set(HEALTH_CHECK_KEY, "ping")
            .then(redisTemplate.opsForValue().get(HEALTH_CHECK_KEY))
            .timeout(HEALTH_CHECK_TIMEOUT)
            .map(value -> {
                long responseTime = System.currentTimeMillis() - startTime;
                
                if ("ping".equals(value)) {
                    log.debug("Redis health check successful - Response time: {}ms", responseTime);
                    return Health.up()
                        .withDetail("status", "UP")
                        .withDetail("responseTime", responseTime + "ms")
                        .withDetail("connection", "active")
                        .build();
                } else {
                    log.warn("Redis health check failed - Unexpected response: {}", value);
                    return Health.down()
                        .withDetail("status", "DOWN")
                        .withDetail("error", "Unexpected response: " + value)
                        .withDetail("responseTime", responseTime + "ms")
                        .build();
                }
            })
            .onErrorResume(throwable -> {
                long responseTime = System.currentTimeMillis() - startTime;
                log.error("Redis health check failed - Response time: {}ms", responseTime, throwable);
                
                return Mono.just(Health.down()
                    .withDetail("status", "DOWN")
                    .withDetail("error", throwable.getMessage())
                    .withDetail("responseTime", responseTime + "ms")
                    .withDetail("connection", "failed")
                    .build());
            })
            .doFinally(signalType -> {
                // Clean up health check key (fire and forget)
                redisTemplate.delete(HEALTH_CHECK_KEY)
                    .onErrorResume(error -> {
                        log.debug("Failed to clean up health check key: {}", error.getMessage());
                        return Mono.empty();
                    })
                    .subscribe();
            });
    }
}