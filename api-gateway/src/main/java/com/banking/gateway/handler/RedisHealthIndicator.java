package com.banking.gateway.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Redis Health Indicator
 * 
 * Custom health check for Redis connectivity.
 * Used by Kubernetes liveness and readiness probes.
 * 
 * Health Check:
 * - Executes PING command to Redis
 * - Timeout: 2 seconds
 * - Success: Returns UP status
 * - Failure: Returns DOWN status with error details
 * 
 * Kubernetes Integration:
 * - Liveness probe: /actuator/health/liveness
 * - Readiness probe: /actuator/health/readiness
 * - Redis health affects readiness (not liveness)
 * 
 * Health Status:
 * - UP: Redis is reachable and responding
 * - DOWN: Redis is unreachable or not responding
 * - UNKNOWN: Health check timed out or error occurred
 * 
 * @author Banking Platform Team
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisHealthIndicator implements ReactiveHealthIndicator {

    private final ReactiveRedisTemplate<String, String> redisTemplate;

    @Override
    public Mono<Health> health() {
        return redisTemplate.getConnectionFactory()
                .getReactiveConnection()
                .serverCommands()
                .ping()
                .timeout(Duration.ofSeconds(2))
                .map(response -> {
                    log.debug("Redis health check: {}", response);
                    return Health.up()
                            .withDetail("redis", "available")
                            .withDetail("response", response)
                            .build();
                })
                .onErrorResume(error -> {
                    log.error("Redis health check failed", error);
                    return Mono.just(Health.down()
                            .withDetail("redis", "unavailable")
                            .withDetail("error", error.getMessage())
                            .build());
                });
    }
}
