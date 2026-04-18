package com.banking.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import java.util.List;

/**
 * Configuration properties for the Banking API Gateway.
 * 
 * Binds to 'banking.gateway' configuration namespace and provides
 * type-safe access to all gateway configuration values.
 * 
 * All properties use environment variable injection with sensible defaults.
 * Production values are injected via OpenShift ConfigMap and Secrets.
 * 
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
@Validated
@ConfigurationProperties(prefix = "banking.gateway")
public class GatewayProperties {

    /**
     * JWT configuration for token validation
     */
    @Valid
    private Jwt jwt = new Jwt();

    /**
     * Rate limiting configuration
     */
    @Valid
    private RateLimit rateLimit = new RateLimit();

    /**
     * CORS configuration
     */
    @Valid
    private Cors cors = new Cors();

    /**
     * Downstream service URLs
     */
    @Valid
    private Services services = new Services();

    @Data
    public static class Jwt {
        /**
         * RSA public key in PEM format for JWT signature validation.
         * Must match the private key used by Identity Service.
         */
        @NotBlank(message = "JWT public key is required")
        private String publicKey;

        /**
         * Expected JWT issuer (iss claim)
         */
        @NotBlank(message = "JWT issuer is required")
        private String issuer;

        /**
         * Expected JWT audience (aud claim)
         */
        @NotEmpty(message = "JWT audience is required")
        private List<String> audience;
    }

    @Data
    public static class RateLimit {
        /**
         * Maximum requests per minute per authenticated user
         */
        @Positive(message = "Requests per minute per user must be positive")
        private int requestsPerMinutePerUser = 100;

        /**
         * Maximum requests per minute per IP address
         */
        @Positive(message = "Requests per minute per IP must be positive")
        private int requestsPerMinutePerIp = 200;

        /**
         * Sliding window size in seconds
         */
        @Positive(message = "Window size seconds must be positive")
        private int windowSizeSeconds = 60;
    }

    @Data
    public static class Cors {
        /**
         * Allowed origins for CORS requests
         */
        @NotEmpty(message = "CORS allowed origins is required")
        private List<String> allowedOrigins;

        /**
         * Allowed HTTP methods
         */
        private List<String> allowedMethods = List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");

        /**
         * Allowed headers
         */
        private List<String> allowedHeaders = List.of(
            "Authorization", "Content-Type", "X-Requested-With", 
            "Idempotency-Key", "Accept", "Accept-Language", "X-Device-Id"
        );

        /**
         * Whether to allow credentials in CORS requests
         */
        private boolean allowCredentials = true;

        /**
         * CORS preflight cache duration in seconds
         */
        @Positive(message = "CORS max age seconds must be positive")
        private int maxAgeSeconds = 3600;
    }

    @Data
    public static class Services {
        @NotBlank(message = "Identity service URL is required")
        private String identityServiceUrl;

        @NotBlank(message = "User service URL is required")
        private String userServiceUrl;

        @NotBlank(message = "Account service URL is required")
        private String accountServiceUrl;

        @NotBlank(message = "Transaction service URL is required")
        private String transactionServiceUrl;

        @NotBlank(message = "Fraud service URL is required")
        private String fraudServiceUrl;

        @NotBlank(message = "Audit service URL is required")
        private String auditServiceUrl;

        @NotBlank(message = "Notification service URL is required")
        private String notificationServiceUrl;

        @NotBlank(message = "Chat service URL is required")
        private String chatServiceUrl;

        @NotBlank(message = "AI orchestration service URL is required")
        private String aiOrchestrationServiceUrl;

        @NotBlank(message = "Document ingestion service URL is required")
        private String documentIngestionServiceUrl;

        @NotBlank(message = "Analytics service URL is required")
        private String analyticsServiceUrl;

        @NotBlank(message = "Statement service URL is required")
        private String statementServiceUrl;
    }
}