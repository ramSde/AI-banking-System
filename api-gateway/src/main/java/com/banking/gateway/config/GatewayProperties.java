package com.banking.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Gateway Configuration Properties
 * 
 * Binds all gateway.* properties from application.yml to strongly-typed Java objects.
 * This provides type-safe access to configuration and enables IDE autocomplete.
 * 
 * Properties include:
 * - JWT configuration (public key, issuer, audience)
 * - Rate limiting settings (per-user and per-IP limits)
 * - Service route mappings (URI and path for each microservice)
 * 
 * @author Banking Platform Team
 * @version 1.0.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "gateway")
public class GatewayProperties {

    private JwtProperties jwt = new JwtProperties();
    private RateLimitProperties rateLimit = new RateLimitProperties();
    private Map<String, RouteProperties> routes = new HashMap<>();

    @Data
    public static class JwtProperties {
        private String publicKey;
        private String issuer;
        private String audience;
    }

    @Data
    public static class RateLimitProperties {
        private boolean enabled = true;
        private int perUserLimit = 100;
        private int perIpLimit = 200;
        private int windowSizeSeconds = 60;
    }

    @Data
    public static class RouteProperties {
        private String uri;
        private String path;
    }
}
