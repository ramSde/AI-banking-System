package com.banking.gateway.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Gateway routing configuration for all downstream microservices.
 * 
 * This configuration defines routes for all banking platform services with:
 * - Path-based routing with /api/v1/{service} prefix
 * - Circuit breaker integration for fault tolerance
 * - Request/response size limits
 * - Timeout configuration per service type
 * - Load balancing for multiple service instances
 * 
 * Route Design Principles:
 * - RESTful path structure: /api/v1/{domain}/{resource}
 * - Consistent error handling across all routes
 * - Observability through request/response logging
 * - Security through JWT validation (applied via filters)
 * 
 * @author Banking Platform Team
 * @version 1.0.0
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class GatewayRoutingConfig {

    private final GatewayProperties gatewayProperties;

    /**
     * Configure all routes for banking platform microservices.
     * 
     * Each route includes:
     * - Path predicate for request matching
     * - URI for downstream service
     * - Circuit breaker with fallback
     * - Request timeout based on service type
     * - Strip path prefix for clean downstream URLs
     * 
     * @param builder RouteLocatorBuilder for fluent route configuration
     * @return RouteLocator with all configured routes
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        log.info("Configuring gateway routes for {} services", 
                gatewayProperties.getServices().asMap().size());

        return builder.routes()
                // Identity Service - Authentication & Authorization
                .route("identity-service", r -> r
                        .path("/api/v1/auth/**", "/api/v1/identity/**")
                        .filters(f -> f
                                .stripPrefix(2) // Remove /api/v1 prefix
                                .circuitBreaker(c -> c
                                        .name("identity-service-cb")
                                        .fallbackUri("forward:/fallback/identity"))
                                .requestSize(1024 * 1024) // 1MB limit for auth requests
                        )
                        .uri(gatewayProperties.getServices().getIdentityServiceUrl())
                        .metadata("timeout", 10000) // 10s for auth operations
                        .metadata("service", "identity")
                )

                // User Service - User profiles and preferences
                .route("user-service", r -> r
                        .path("/api/v1/users/**", "/api/v1/profiles/**")
                        .filters(f -> f
                                .stripPrefix(2)
                                .circuitBreaker(c -> c
                                        .name("user-service-cb")
                                        .fallbackUri("forward:/fallback/user"))
                                .requestSize(5 * 1024 * 1024) // 5MB for profile images
                        )
                        .uri(gatewayProperties.getServices().getUserServiceUrl())
                        .metadata("timeout", 5000) // 5s for user operations
                        .metadata("service", "user")
                )

                // Account Service - Account management
                .route("account-service", r -> r
                        .path("/api/v1/accounts/**")
                        .filters(f -> f
                                .stripPrefix(2)
                                .circuitBreaker(c -> c
                                        .name("account-service-cb")
                                        .fallbackUri("forward:/fallback/account"))
                                .requestSize(1024 * 1024) // 1MB for account operations
                        )
                        .uri(gatewayProperties.getServices().getAccountServiceUrl())
                        .metadata("timeout", 8000) // 8s for account operations
                        .metadata("service", "account")
                )

                // Transaction Service - Payment processing
                .route("transaction-service", r -> r
                        .path("/api/v1/transactions/**", "/api/v1/payments/**", "/api/v1/transfers/**")
                        .filters(f -> f
                                .stripPrefix(2)
                                .circuitBreaker(c -> c
                                        .name("transaction-service-cb")
                                        .fallbackUri("forward:/fallback/transaction"))
                                .requestSize(1024 * 1024) // 1MB for transaction data
                        )
                        .uri(gatewayProperties.getServices().getTransactionServiceUrl())
                        .metadata("timeout", 15000) // 15s for payment processing
                        .metadata("service", "transaction")
                )

                // Fraud Detection Service - Risk analysis
                .route("fraud-service", r -> r
                        .path("/api/v1/fraud/**", "/api/v1/risk/**")
                        .filters(f -> f
                                .stripPrefix(2)
                                .circuitBreaker(c -> c
                                        .name("fraud-service-cb")
                                        .fallbackUri("forward:/fallback/fraud"))
                                .requestSize(1024 * 1024) // 1MB for fraud analysis
                        )
                        .uri(gatewayProperties.getServices().getFraudServiceUrl())
                        .metadata("timeout", 12000) // 12s for ML-based analysis
                        .metadata("service", "fraud")
                )

                // Audit Service - Compliance and logging
                .route("audit-service", r -> r
                        .path("/api/v1/audit/**", "/api/v1/compliance/**")
                        .filters(f -> f
                                .stripPrefix(2)
                                .circuitBreaker(c -> c
                                        .name("audit-service-cb")
                                        .fallbackUri("forward:/fallback/audit"))
                                .requestSize(2 * 1024 * 1024) // 2MB for audit logs
                        )
                        .uri(gatewayProperties.getServices().getAuditServiceUrl())
                        .metadata("timeout", 5000) // 5s for audit operations
                        .metadata("service", "audit")
                )

                // Notification Service - Email, SMS, Push
                .route("notification-service", r -> r
                        .path("/api/v1/notifications/**", "/api/v1/alerts/**")
                        .filters(f -> f
                                .stripPrefix(2)
                                .circuitBreaker(c -> c
                                        .name("notification-service-cb")
                                        .fallbackUri("forward:/fallback/notification"))
                                .requestSize(1024 * 1024) // 1MB for notification content
                        )
                        .uri(gatewayProperties.getServices().getNotificationServiceUrl())
                        .metadata("timeout", 8000) // 8s for external API calls
                        .metadata("service", "notification")
                )

                // Chat Service - AI-powered conversations
                .route("chat-service", r -> r
                        .path("/api/v1/chat/**", "/api/v1/conversations/**")
                        .filters(f -> f
                                .stripPrefix(2)
                                .circuitBreaker(c -> c
                                        .name("chat-service-cb")
                                        .fallbackUri("forward:/fallback/chat"))
                                .requestSize(2 * 1024 * 1024) // 2MB for chat history
                        )
                        .uri(gatewayProperties.getServices().getChatServiceUrl())
                        .metadata("timeout", 30000) // 30s for AI processing
                        .metadata("service", "chat")
                )

                // AI Orchestration Service - Multi-model AI coordination
                .route("ai-orchestration-service", r -> r
                        .path("/api/v1/ai/**", "/api/v1/insights/**", "/api/v1/recommendations/**")
                        .filters(f -> f
                                .stripPrefix(2)
                                .circuitBreaker(c -> c
                                        .name("ai-orchestration-service-cb")
                                        .fallbackUri("forward:/fallback/ai"))
                                .requestSize(5 * 1024 * 1024) // 5MB for AI context
                        )
                        .uri(gatewayProperties.getServices().getAiOrchestrationServiceUrl())
                        .metadata("timeout", 60000) // 60s for complex AI operations
                        .metadata("service", "ai")
                )

                // Document Ingestion Service - PDF/image processing
                .route("document-ingestion-service", r -> r
                        .path("/api/v1/documents/**", "/api/v1/uploads/**")
                        .filters(f -> f
                                .stripPrefix(2)
                                .circuitBreaker(c -> c
                                        .name("document-ingestion-service-cb")
                                        .fallbackUri("forward:/fallback/document"))
                                .requestSize(50 * 1024 * 1024) // 50MB for document uploads
                        )
                        .uri(gatewayProperties.getServices().getDocumentIngestionServiceUrl())
                        .metadata("timeout", 45000) // 45s for document processing
                        .metadata("service", "document")
                )

                // Analytics Service - Financial insights and reporting
                .route("analytics-service", r -> r
                        .path("/api/v1/analytics/**", "/api/v1/reports/**", "/api/v1/dashboards/**")
                        .filters(f -> f
                                .stripPrefix(2)
                                .circuitBreaker(c -> c
                                        .name("analytics-service-cb")
                                        .fallbackUri("forward:/fallback/analytics"))
                                .requestSize(2 * 1024 * 1024) // 2MB for analytics queries
                        )
                        .uri(gatewayProperties.getServices().getAnalyticsServiceUrl())
                        .metadata("timeout", 20000) // 20s for complex analytics
                        .metadata("service", "analytics")
                )

                // Statement Service - PDF generation and downloads
                .route("statement-service", r -> r
                        .path("/api/v1/statements/**", "/api/v1/exports/**")
                        .filters(f -> f
                                .stripPrefix(2)
                                .circuitBreaker(c -> c
                                        .name("statement-service-cb")
                                        .fallbackUri("forward:/fallback/statement"))
                                .requestSize(1024 * 1024) // 1MB for statement requests
                        )
                        .uri(gatewayProperties.getServices().getStatementServiceUrl())
                        .metadata("timeout", 25000) // 25s for PDF generation
                        .metadata("service", "statement")
                )

                .build();
    }
}