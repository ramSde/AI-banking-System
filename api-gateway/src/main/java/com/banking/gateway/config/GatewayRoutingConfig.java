package com.banking.gateway.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Cloud Gateway routing configuration.
 * 
 * Defines all routes to downstream microservices with:
 * - Path-based routing predicates
 * - Circuit breaker filters for fault tolerance
 * - Retry policies with exponential backoff
 * - Request/response transformation filters
 * 
 * Routes are organized by business domain and include fallback handling
 * for service unavailability scenarios.
 * 
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class GatewayRoutingConfig {

    private final GatewayProperties gatewayProperties;

    /**
     * Configures all routes to downstream banking services.
     * 
     * Each route includes:
     * - Path predicate for request matching
     * - Circuit breaker with fallback URI
     * - Retry filter with exponential backoff
     * - Request ID injection for tracing
     * 
     * @param builder RouteLocatorBuilder for fluent route configuration
     * @return Configured RouteLocator with all banking service routes
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        log.info("Configuring API Gateway routes for {} services", 12);
        
        return builder.routes()
            // ── IDENTITY & AUTHENTICATION ROUTES ────────────────────────────
            .route("identity-service", r -> r
                .path("/api/v1/auth/**", "/api/v1/identity/**")
                .filters(f -> f
                    .circuitBreaker(config -> config
                        .setName("identity-service-cb")
                        .setFallbackUri("forward:/fallback/identity"))
                    .retry(config -> config
                        .setRetries(3)
                        .setBackoff(java.time.Duration.ofMillis(100), 
                                   java.time.Duration.ofMillis(1000), 2, false))
                    .addRequestHeader("X-Gateway-Source", "api-gateway")
                    .addRequestHeader("X-Request-ID", "#{T(java.util.UUID).randomUUID().toString()}"))
                .uri(gatewayProperties.getServices().getIdentityServiceUrl()))

            // ── USER MANAGEMENT ROUTES ──────────────────────────────────────
            .route("user-service", r -> r
                .path("/api/v1/users/**", "/api/v1/profiles/**")
                .filters(f -> f
                    .circuitBreaker(config -> config
                        .setName("user-service-cb")
                        .setFallbackUri("forward:/fallback/user"))
                    .retry(config -> config
                        .setRetries(3)
                        .setBackoff(java.time.Duration.ofMillis(100), 
                                   java.time.Duration.ofMillis(1000), 2, false))
                    .addRequestHeader("X-Gateway-Source", "api-gateway")
                    .addRequestHeader("X-Request-ID", "#{T(java.util.UUID).randomUUID().toString()}"))
                .uri(gatewayProperties.getServices().getUserServiceUrl()))

            // ── ACCOUNT MANAGEMENT ROUTES ───────────────────────────────────
            .route("account-service", r -> r
                .path("/api/v1/accounts/**")
                .filters(f -> f
                    .circuitBreaker(config -> config
                        .setName("account-service-cb")
                        .setFallbackUri("forward:/fallback/account"))
                    .retry(config -> config
                        .setRetries(3)
                        .setBackoff(java.time.Duration.ofMillis(100), 
                                   java.time.Duration.ofMillis(1000), 2, false))
                    .addRequestHeader("X-Gateway-Source", "api-gateway")
                    .addRequestHeader("X-Request-ID", "#{T(java.util.UUID).randomUUID().toString()}"))
                .uri(gatewayProperties.getServices().getAccountServiceUrl()))

            // ── TRANSACTION ROUTES ──────────────────────────────────────────
            .route("transaction-service", r -> r
                .path("/api/v1/transactions/**", "/api/v1/transfers/**")
                .filters(f -> f
                    .circuitBreaker(config -> config
                        .setName("transaction-service-cb")
                        .setFallbackUri("forward:/fallback/transaction"))
                    .retry(config -> config
                        .setRetries(2)  // Fewer retries for financial operations
                        .setBackoff(java.time.Duration.ofMillis(200), 
                                   java.time.Duration.ofMillis(2000), 2, false))
                    .addRequestHeader("X-Gateway-Source", "api-gateway")
                    .addRequestHeader("X-Request-ID", "#{T(java.util.UUID).randomUUID().toString()}"))
                .uri(gatewayProperties.getServices().getTransactionServiceUrl()))

            // ── FRAUD DETECTION ROUTES ──────────────────────────────────────
            .route("fraud-service", r -> r
                .path("/api/v1/fraud/**")
                .filters(f -> f
                    .circuitBreaker(config -> config
                        .setName("fraud-service-cb")
                        .setFallbackUri("forward:/fallback/fraud"))
                    .retry(config -> config
                        .setRetries(2)
                        .setBackoff(java.time.Duration.ofMillis(100), 
                                   java.time.Duration.ofMillis(500), 2, false))
                    .addRequestHeader("X-Gateway-Source", "api-gateway")
                    .addRequestHeader("X-Request-ID", "#{T(java.util.UUID).randomUUID().toString()}"))
                .uri(gatewayProperties.getServices().getFraudServiceUrl()))

            // ── AUDIT ROUTES ────────────────────────────────────────────────
            .route("audit-service", r -> r
                .path("/api/v1/audit/**")
                .filters(f -> f
                    .circuitBreaker(config -> config
                        .setName("audit-service-cb")
                        .setFallbackUri("forward:/fallback/audit"))
                    .retry(config -> config
                        .setRetries(3)
                        .setBackoff(java.time.Duration.ofMillis(100), 
                                   java.time.Duration.ofMillis(1000), 2, false))
                    .addRequestHeader("X-Gateway-Source", "api-gateway")
                    .addRequestHeader("X-Request-ID", "#{T(java.util.UUID).randomUUID().toString()}"))
                .uri(gatewayProperties.getServices().getAuditServiceUrl()))

            // ── NOTIFICATION ROUTES ─────────────────────────────────────────
            .route("notification-service", r -> r
                .path("/api/v1/notifications/**")
                .filters(f -> f
                    .circuitBreaker(config -> config
                        .setName("notification-service-cb")
                        .setFallbackUri("forward:/fallback/notification"))
                    .retry(config -> config
                        .setRetries(3)
                        .setBackoff(java.time.Duration.ofMillis(100), 
                                   java.time.Duration.ofMillis(1000), 2, false))
                    .addRequestHeader("X-Gateway-Source", "api-gateway")
                    .addRequestHeader("X-Request-ID", "#{T(java.util.UUID).randomUUID().toString()}"))
                .uri(gatewayProperties.getServices().getNotificationServiceUrl()))

            // ── CHAT ROUTES ─────────────────────────────────────────────────
            .route("chat-service", r -> r
                .path("/api/v1/chat/**")
                .filters(f -> f
                    .circuitBreaker(config -> config
                        .setName("chat-service-cb")
                        .setFallbackUri("forward:/fallback/chat"))
                    .retry(config -> config
                        .setRetries(2)
                        .setBackoff(java.time.Duration.ofMillis(200), 
                                   java.time.Duration.ofMillis(2000), 2, false))
                    .addRequestHeader("X-Gateway-Source", "api-gateway")
                    .addRequestHeader("X-Request-ID", "#{T(java.util.UUID).randomUUID().toString()}"))
                .uri(gatewayProperties.getServices().getChatServiceUrl()))

            // ── AI ORCHESTRATION ROUTES ─────────────────────────────────────
            .route("ai-orchestration-service", r -> r
                .path("/api/v1/ai/**", "/api/v1/insights/**")
                .filters(f -> f
                    .circuitBreaker(config -> config
                        .setName("ai-orchestration-service-cb")
                        .setFallbackUri("forward:/fallback/ai"))
                    .retry(config -> config
                        .setRetries(2)
                        .setBackoff(java.time.Duration.ofMillis(500), 
                                   java.time.Duration.ofMillis(5000), 2, false))
                    .addRequestHeader("X-Gateway-Source", "api-gateway")
                    .addRequestHeader("X-Request-ID", "#{T(java.util.UUID).randomUUID().toString()}"))
                .uri(gatewayProperties.getServices().getAiOrchestrationServiceUrl()))

            // ── DOCUMENT INGESTION ROUTES ───────────────────────────────────
            .route("document-ingestion-service", r -> r
                .path("/api/v1/documents/**")
                .filters(f -> f
                    .circuitBreaker(config -> config
                        .setName("document-ingestion-service-cb")
                        .setFallbackUri("forward:/fallback/document"))
                    .retry(config -> config
                        .setRetries(2)
                        .setBackoff(java.time.Duration.ofMillis(200), 
                                   java.time.Duration.ofMillis(2000), 2, false))
                    .addRequestHeader("X-Gateway-Source", "api-gateway")
                    .addRequestHeader("X-Request-ID", "#{T(java.util.UUID).randomUUID().toString()}"))
                .uri(gatewayProperties.getServices().getDocumentIngestionServiceUrl()))

            // ── ANALYTICS ROUTES ────────────────────────────────────────────
            .route("analytics-service", r -> r
                .path("/api/v1/analytics/**", "/api/v1/reports/**")
                .filters(f -> f
                    .circuitBreaker(config -> config
                        .setName("analytics-service-cb")
                        .setFallbackUri("forward:/fallback/analytics"))
                    .retry(config -> config
                        .setRetries(3)
                        .setBackoff(java.time.Duration.ofMillis(100), 
                                   java.time.Duration.ofMillis(1000), 2, false))
                    .addRequestHeader("X-Gateway-Source", "api-gateway")
                    .addRequestHeader("X-Request-ID", "#{T(java.util.UUID).randomUUID().toString()}"))
                .uri(gatewayProperties.getServices().getAnalyticsServiceUrl()))

            // ── STATEMENT ROUTES ────────────────────────────────────────────
            .route("statement-service", r -> r
                .path("/api/v1/statements/**")
                .filters(f -> f
                    .circuitBreaker(config -> config
                        .setName("statement-service-cb")
                        .setFallbackUri("forward:/fallback/statement"))
                    .retry(config -> config
                        .setRetries(3)
                        .setBackoff(java.time.Duration.ofMillis(100), 
                                   java.time.Duration.ofMillis(1000), 2, false))
                    .addRequestHeader("X-Gateway-Source", "api-gateway")
                    .addRequestHeader("X-Request-ID", "#{T(java.util.UUID).randomUUID().toString()}"))
                .uri(gatewayProperties.getServices().getStatementServiceUrl()))

            .build();
    }
}