package com.banking.gateway.config;

import com.banking.gateway.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * Security configuration for the API Gateway.
 * 
 * This configuration provides:
 * - JWT-based stateless authentication
 * - CORS policy for web clients
 * - Public endpoints for health checks and authentication
 * - Protected endpoints requiring valid JWT tokens
 * - Security headers for XSS and clickjacking protection
 * 
 * Security Architecture:
 * - Stateless design (no server-side sessions)
 * - JWT RS256 signature validation
 * - Role-based access control (RBAC)
 * - Request/response security headers
 * - CSRF disabled (stateless REST API)
 * 
 * @author Banking Platform Team
 * @version 1.0.0
 */
@Slf4j
@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final GatewayProperties gatewayProperties;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Configure the security filter chain for reactive web security.
     * 
     * Security Rules:
     * - Public: health checks, authentication endpoints, CORS preflight
     * - Protected: all other API endpoints require valid JWT
     * - Stateless: no server-side session management
     * - CORS: configured for web client access
     * 
     * @param http ServerHttpSecurity for reactive security configuration
     * @return SecurityWebFilterChain with all security rules
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        log.info("Configuring security filter chain with JWT authentication");

        return http
                // Disable CSRF (stateless REST API)
                .csrf(csrf -> csrf.disable())
                
                // Disable form login (JWT-based authentication)
                .formLogin(form -> form.disable())
                
                // Disable HTTP Basic (JWT-based authentication)
                .httpBasic(basic -> basic.disable())
                
                // Stateless session management
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                
                // CORS configuration
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                
                // Authorization rules
                .authorizeExchange(exchanges -> exchanges
                        // Public endpoints - no authentication required
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll() // CORS preflight
                        .pathMatchers("/actuator/health/**").permitAll() // Health checks
                        .pathMatchers("/actuator/info").permitAll() // Info endpoint
                        .pathMatchers("/actuator/prometheus").permitAll() // Metrics (secured at infra level)
                        .pathMatchers("/api/v1/auth/login").permitAll() // Login
                        .pathMatchers("/api/v1/auth/register").permitAll() // Registration
                        .pathMatchers("/api/v1/auth/refresh").permitAll() // Token refresh
                        .pathMatchers("/api/v1/auth/forgot-password").permitAll() // Password reset
                        .pathMatchers("/api/v1/auth/verify-otp").permitAll() // OTP verification
                        .pathMatchers("/fallback/**").permitAll() // Circuit breaker fallbacks
                        
                        // Protected endpoints - require authentication
                        .anyExchange().authenticated()
                )
                
                // Add JWT authentication filter
                .addFilterBefore(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                
                // Security headers
                .headers(headers -> headers
                        .frameOptions(frame -> frame.deny()) // Prevent clickjacking
                        .contentTypeOptions(content -> content.and()) // Prevent MIME sniffing
                        .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                                .maxAgeInSeconds(31536000) // 1 year
                                .includeSubdomains(true)
                                .preload(true)
                        )
                )
                
                .build();
    }

    /**
     * Configure CORS for web client access.
     * 
     * CORS Policy:
     * - Allowed origins: configured per environment (no wildcard in prod)
     * - Allowed methods: standard REST methods + OPTIONS
     * - Allowed headers: authentication, content type, custom headers
     * - Credentials: allowed for authentication cookies/headers
     * - Max age: 1 hour for preflight cache
     * 
     * @return CorsConfigurationSource with CORS policy
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        log.info("Configuring CORS with {} allowed origins", 
                gatewayProperties.getCors().getAllowedOrigins().size());

        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allowed origins (never wildcard in production)
        configuration.setAllowedOrigins(gatewayProperties.getCors().getAllowedOrigins());
        
        // Allowed methods
        configuration.setAllowedMethods(gatewayProperties.getCors().getAllowedMethods());
        
        // Allowed headers
        configuration.setAllowedHeaders(gatewayProperties.getCors().getAllowedHeaders());
        
        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(gatewayProperties.getCors().isAllowCredentials());
        
        // Preflight cache duration
        configuration.setMaxAge((long) gatewayProperties.getCors().getMaxAgeSeconds());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}