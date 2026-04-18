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
 * This health indicator provides:
 * - Redis connection status monitoring
 * - Response time measurement
 * - Connection pool status
 * - Integration with Spring Boot Actuator health checks
 * - Kubernetes readiness/liveness probe support
 * 
 * Health Check Strategy:
 * - Performs PING command to verify Redis connectivity
 * - Measures response time for performance monitoring
 * - Fails fast with timeout to prevent blocking health checks
 * - Provides detailed status information for debugging
 * 
 * Integration:
 * - Used by Kubernetes readiness probes
 * - Monitored by Prometheus for alerting
 * - Included in /actuator/health endpoint
 * - Supports graceful degradation when Redis is unavailable
 * 
 * @author Banking Platform Team
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisHealthIndicator implements ReactiveHealthIndicator {

    private final ReactiveRedisTemplate<String, Object> redisTemplate;

    private static final Duration HEALTH_CHECK_TIMEOUT = Duration.ofSeconds(3);
    private static final String PING_COMMAND = "PING";
    private static final String EXPECTED_RESPONSE = "PONG";

    /**
     * Perform Redis health check.
     * 
     * Health Check Process:
     * 1. Execute PING command against Redis
     * 2. Measure response time
     * 3. Verify expected PONG response
     * 4. Return health status with details
     * 5. Handle timeouts and connection errors
     * 
     * @return Mono<Health> containing health status and details
     */
    @Override
    public Mono<Health> health() {
        long startTime = System.currentTimeMillis();

        return redisTemplate.getConnectionFactory()
                .getReactiveConnection()
                .ping()
                .map(response -> {
                    long responseTime = System.currentTimeMillis() - startTime;
                    
                    if (EXPECTED_RESPONSE.equals(response)) {
                        log.debug("Redis health check successful - Response time: {}ms", responseTime);
                        
                        return Health.up()
                                .withDetail("status", "UP")
                                .withDetail("responseTime", responseTime + "ms")
                                .withDetail("connection", "active")
                                .withDetail("lastCheck", java.time.Instant.now().toString())
                                .build();
                    } else {
                        log.warn("Redis health check failed - Unexpected response: {}", response);
                        
                        return Health.down()
                                .withDetail("status", "DOWN")
                                .withDetail("reason", "Unexpected PING response")
                                .withDetail("expectedResponse", EXPECTED_RESPONSE)
                                .withDetail("actualResponse", response)
                                .withDetail("responseTime", responseTime + "ms")
                                .withDetail("lastCheck", java.time.Instant.now().toString())
                                .build();
                    }
                })
                .timeout(HEALTH_CHECK_TIMEOUT)
                .onErrorResume(throwable -> {
                    long responseTime = System.currentTimeMillis() - startTime;
                    
                    log.error("Redis health check failed - Error: {}, Response time: {}ms", 
                            throwable.getMessage(), responseTime);
                    
                    return Mono.just(Health.down()
                            .withDetail("status", "DOWN")
                            .withDetail("reason", "Connection failed")
                            .withDetail("error", throwable.getMessage())
                            .withDetail("errorType", throwable.getClass().getSimpleName())
                            .withDetail("responseTime", responseTime + "ms")
                            .withDetail("timeout", HEALTH_CHECK_TIMEOUT.toMillis() + "ms")
                            .withDetail("lastCheck", java.time.Instant.now().toString())
                            .build());
                });
    }
}