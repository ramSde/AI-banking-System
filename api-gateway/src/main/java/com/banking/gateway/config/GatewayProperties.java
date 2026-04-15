package com.banking.gateway.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

/**
 * Configuration properties for the API Gateway.
 * 
 * All properties are externalized via environment variables following the
 * ${ENV_VAR:default_value} pattern. This ensures zero hardcoded configuration
 * and supports different environments (dev, staging, prod).
 * 
 * Validation ensures all critical properties are present and valid at startup.
 * 
 * @author Banking Platform Team
 * @version 1.0.0
 */
@Data
@Validated
@ConfigurationProperties(prefix = "banking.gateway")
public class GatewayProperties {

    @Valid
    @NotNull
    private Jwt jwt = new Jwt();

    @Valid
    @NotNull
    private RateLimit rateLimit = new RateLimit();

    @Valid
    @NotNull
    private Cors cors = new Cors();

    @Valid
    @NotNull
    private Services services = new Services();

    /**
     * JWT configuration for token validation.
     */
    @Data
    public static class Jwt {
        
        /**
         * RSA public key in PEM format for JWT signature verification.
         * Must match the private key used by Identity Service for signing.
         * 
         * In production: injected from Kubernetes Secret or HashiCorp Vault.
         * NEVER commit actual key values to source control.
         */
        @NotBlank(message = "JWT public key is required")
        private String publicKey;

        /**
         * Expected JWT issuer (iss claim).
         * Must match the issuer configured in Identity Service.
         */
        @NotBlank(message = "JWT issuer is required")
        private String issuer = "banking-platform";

        /**
         * Expected JWT audience (aud claim).
         * Tokens must contain at least one of these audience values.
         */
        @NotEmpty(message = "JWT audience list cannot be empty")
        private List<String> audience = List.of("banking-api");
    }

    /**
     * Rate limiting configuration using Redis sliding window algorithm.
     */
    @Data
    public static class RateLimit {
        
        /**
         * Maximum requests per minute per authenticated user.
         * Applied after successful JWT validation.
         */
        @Min(value = 1, message = "Requests per minute per user must be at least 1")
        private int requestsPerMinutePerUser = 100;

        /**
         * Maximum requests per minute per IP address.
         * Applied before authentication to prevent brute force attacks.
         */
        @Min(value = 1, message = "Requests per minute per IP must be at least 1")
        private int requestsPerMinutePerIp = 200;

        /**
         * Sliding window size in seconds.
         * Determines the granularity of rate limiting calculations.
         */
        @Min(value = 1, message = "Window size must be at least 1 second")
        private int windowSizeSeconds = 60;
    }

    /**
     * CORS configuration for web client access.
     */
    @Data
    public static class Cors {
        
        /**
         * Allowed origins for CORS requests.
         * In production: specific domains only, never wildcard (*).
         */
        @NotEmpty(message = "CORS allowed origins cannot be empty")
        private List<String> allowedOrigins = List.of("http://localhost:3000");

        /**
         * Allowed HTTP methods for CORS requests.
         */
        @NotEmpty(message = "CORS allowed methods cannot be empty")
        private List<String> allowedMethods = List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");

        /**
         * Allowed headers for CORS requests.
         */
        @NotEmpty(message = "CORS allowed headers cannot be empty")
        private List<String> allowedHeaders = List.of(
            "Authorization", "Content-Type", "X-Requested-With", 
            "Idempotency-Key", "Accept", "Accept-Language", "X-Device-Id"
        );

        /**
         * Whether to allow credentials (cookies, authorization headers) in CORS requests.
         */
        private boolean allowCredentials = true;

        /**
         * Maximum age in seconds for CORS preflight cache.
         */
        @Min(value = 0, message = "CORS max age cannot be negative")
        private int maxAgeSeconds = 3600;
    }

    /**
     * Downstream service URLs for routing configuration.
     * 
     * In Kubernetes: uses DNS format service-name.namespace.svc.cluster.local:port
     * In local dev: uses localhost with different ports per service
     */
    @Data
    public static class Services {
        
        @NotBlank(message = "Identity service URL is required")
        private String identityServiceUrl = "http://localhost:8082";

        @NotBlank(message = "User service URL is required")
        private String userServiceUrl = "http://localhost:8083";

        @NotBlank(message = "Account service URL is required")
        private String accountServiceUrl = "http://localhost:8084";

        @NotBlank(message = "Transaction service URL is required")
        private String transactionServiceUrl = "http://localhost:8085";

        @NotBlank(message = "Fraud service URL is required")
        private String fraudServiceUrl = "http://localhost:8086";

        @NotBlank(message = "Audit service URL is required")
        private String auditServiceUrl = "http://localhost:8087";

        @NotBlank(message = "Notification service URL is required")
        private String notificationServiceUrl = "http://localhost:8088";

        @NotBlank(message = "Chat service URL is required")
        private String chatServiceUrl = "http://localhost:8089";

        @NotBlank(message = "AI orchestration service URL is required")
        private String aiOrchestrationServiceUrl = "http://localhost:8090";

        @NotBlank(message = "Document ingestion service URL is required")
        private String documentIngestionServiceUrl = "http://localhost:8091";

        @NotBlank(message = "Analytics service URL is required")
        private String analyticsServiceUrl = "http://localhost:8092";

        @NotBlank(message = "Statement service URL is required")
        private String statementServiceUrl = "http://localhost:8093";

        /**
         * Convert to Map for easier iteration in routing configuration.
         */
        public Map<String, String> asMap() {
            return Map.of(
                "identity", identityServiceUrl,
                "users", userServiceUrl,
                "accounts", accountServiceUrl,
                "transactions", transactionServiceUrl,
                "fraud", fraudServiceUrl,
                "audit", auditServiceUrl,
                "notifications", notificationServiceUrl,
                "chat", chatServiceUrl,
                "ai", aiOrchestrationServiceUrl,
                "documents", documentIngestionServiceUrl,
                "analytics", analyticsServiceUrl,
                "statements", statementServiceUrl
            );
        }
    }
}