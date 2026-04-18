package com.banking.gateway.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * Security configuration for Spring Cloud Gateway.
 * 
 * Configures security policies for the API Gateway:
 * - CORS configuration for web client access
 * - CSRF disabled (stateless REST API)
 * - Security headers for protection against common attacks
 * - Public endpoint access rules
 * 
 * Security considerations:
 * - JWT authentication is handled by custom filters
 * - CORS is configured per environment (strict in production)
 * - Security headers prevent XSS, clickjacking, and other attacks
 * - Public endpoints are explicitly defined and secured
 * 
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final GatewayProperties gatewayProperties;

    /**
     * Configures the security filter chain for the gateway.
     * 
     * Security policies:
     * - Disable CSRF (stateless API)
     * - Configure CORS for cross-origin requests
     * - Set security headers
     * - Define public endpoint access rules
     * 
     * @param http ServerHttpSecurity configuration
     * @return Configured SecurityWebFilterChain
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        log.info("Configuring security filter chain for API Gateway");
        
        return http
            // Disable CSRF for stateless REST API
            .csrf(csrf -> csrf.disable())
            
            // Configure CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Configure security headers
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.deny())
                .contentTypeOptions(contentTypeOptions -> contentTypeOptions.and())
                .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                    .maxAgeInSeconds(31536000) // 1 year
                    .includeSubdomains(true)))
            
            // Configure authorization rules
            .authorizeExchange(exchanges -> exchanges
                // Public endpoints - no authentication required
                .pathMatchers("/api/v1/auth/**").permitAll()
                .pathMatchers("/actuator/health/**").permitAll()
                .pathMatchers("/actuator/info").permitAll()
                .pathMatchers("/actuator/prometheus").permitAll()
                .pathMatchers("/fallback/**").permitAll()
                
                // All other endpoints require authentication (handled by JWT filter)
                .anyExchange().authenticated())
            
            .build();
    }

    /**
     * Configures CORS (Cross-Origin Resource Sharing) settings.
     * 
     * CORS configuration is environment-specific:
     * - Development: Permissive for local development
     * - Staging: Restricted to staging domains
     * - Production: Strict whitelist of production domains
     * 
     * @return CORS configuration source
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        log.info("Configuring CORS with allowed origins: {}", 
                gatewayProperties.getCors().getAllowedOrigins());
        
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allowed origins from configuration
        configuration.setAllowedOrigins(gatewayProperties.getCors().getAllowedOrigins());
        
        // Allowed methods
        configuration.setAllowedMethods(gatewayProperties.getCors().getAllowedMethods());
        
        // Allowed headers
        configuration.setAllowedHeaders(gatewayProperties.getCors().getAllowedHeaders());
        
        // Allow credentials (required for JWT cookies if used)
        configuration.setAllowCredentials(gatewayProperties.getCors().isAllowCredentials());
        
        // Preflight cache duration
        configuration.setMaxAge((long) gatewayProperties.getCors().getMaxAgeSeconds());
        
        // Expose headers that clients can access
        configuration.addExposedHeader("X-RateLimit-Limit");
        configuration.addExposedHeader("X-RateLimit-Remaining");
        configuration.addExposedHeader("X-RateLimit-Reset");
        configuration.addExposedHeader("X-Request-ID");
        configuration.addExposedHeader("X-Trace-ID");
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}