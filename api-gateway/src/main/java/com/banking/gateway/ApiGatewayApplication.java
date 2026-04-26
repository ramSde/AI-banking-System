package com.banking.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * API Gateway Application - Entry point for the Banking Platform Gateway
 * 
 * This gateway serves as the single entry point for all client requests to the banking platform.
 * It provides:
 * - Centralized routing to all 30+ microservices
 * - JWT-based authentication and authorization
 * - Rate limiting (per-user and per-IP)
 * - Request/response logging with distributed tracing
 * - Circuit breaker and retry mechanisms
 * - CORS configuration
 * 
 * Technology Stack:
 * - Spring Cloud Gateway (reactive, non-blocking)
 * - Redis (rate limiting, caching)
 * - JWT (RS256 signature verification)
 * - Resilience4j (circuit breaker, retry)
 * - Micrometer + OpenTelemetry (observability)
 * 
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
