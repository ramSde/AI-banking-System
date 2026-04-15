package com.banking.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * API Gateway Application - Entry point for the banking platform gateway service.
 * 
 * This service provides:
 * - JWT-based authentication and authorization
 * - Rate limiting with Redis sliding window
 * - Request routing to downstream microservices
 * - CORS handling for web clients
 * - Comprehensive observability (metrics, tracing, health checks)
 * - Circuit breaker patterns for fault tolerance
 * 
 * Architecture:
 * - Built on Spring Cloud Gateway (reactive/WebFlux)
 * - Stateless design for horizontal scaling
 * - Redis for distributed rate limiting and caching
 * - OpenTelemetry for distributed tracing
 * - Prometheus metrics for monitoring
 * 
 * Security:
 * - JWT RS256 signature validation
 * - Role-based access control (RBAC)
 * - Request/response logging with PII masking
 * - CORS policy enforcement
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