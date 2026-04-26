package com.banking.gateway.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.header.XFrameOptionsServerHttpHeadersWriter;

/**
 * Security Configuration for API Gateway
 * 
 * Configures security policies for the reactive Spring Cloud Gateway.
 * This is a stateless gateway - no session management, all authentication via JWT.
 * 
 * Security Features:
 * - CSRF disabled (stateless REST API with JWT authentication)
 * - CORS configured via application.yml (spring.cloud.gateway.globalcors)
 * - Public endpoints: /actuator/health, /actuator/info, /api-docs, /swagger-ui
 * - Authentication endpoints: /api/v1/auth/** (login, register, refresh)
 * - All other endpoints require authentication (handled by JwtAuthenticationFilter)
 * 
 * HTTP Security Headers:
 * - X-Content-Type-Options: nosniff
 * - X-Frame-Options: DENY
 * - X-XSS-Protection: 1; mode=block
 * - Strict-Transport-Security: max-age=31536000; includeSubDomains (HTTPS only)
 * 
 * Note: JWT validation is performed by JwtAuthenticationFilter (WebFilter)
 * which runs before this SecurityWebFilterChain.
 * 
 * @author Banking Platform Team
 * @version 1.0.0
 */
@Slf4j
@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        log.info("Configuring API Gateway security filter chain");

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(HttpMethod.GET, "/actuator/health", "/actuator/health/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/actuator/info").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api-docs", "/api-docs/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/swagger-ui.html", "/swagger-ui/**").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/v1/auth/register").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/v1/auth/refresh").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/v1/auth/logout").permitAll()
                        .anyExchange().authenticated()
                )
                .headers(headers -> headers
                        .contentTypeOptions(contentTypeOptions -> {})
                        .frameOptions(frameOptions -> 
                                frameOptions.mode(XFrameOptionsServerHttpHeadersWriter.Mode.DENY))
                        .xssProtection(xss -> {})
                        .hsts(hsts -> hsts
                                .maxAge(java.time.Duration.ofDays(365))
                                .includeSubdomains(true))
                )
                .build();
    }
}
