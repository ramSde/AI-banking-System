package com.banking.gateway.filter;

import com.banking.gateway.exception.InvalidTokenException;
import com.banking.gateway.util.JwtValidator;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT Authentication Filter for the API Gateway.
 * 
 * This filter provides:
 * - JWT token extraction from Authorization header
 * - Token validation using RSA public key
 * - Security context population with user details
 * - Role-based authority mapping
 * - Comprehensive error handling for invalid tokens
 * 
 * Filter Behavior:
 * - Processes all requests except public endpoints
 * - Extracts Bearer token from Authorization header
 * - Validates token signature, expiration, and claims
 * - Populates Spring Security context for downstream authorization
 * - Returns 401 Unauthorized for invalid/missing tokens
 * 
 * Security Features:
 * - Stateless authentication (no server-side sessions)
 * - RSA-256 signature verification
 * - Role-based access control (RBAC)
 * - Request tracing with user context
 * 
 * @author Banking Platform Team
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements WebFilter {

    private final JwtValidator jwtValidator;

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTHORIZATION_HEADER = HttpHeaders.AUTHORIZATION;

    /**
     * Filter incoming requests for JWT authentication.
     * 
     * Processing Flow:
     * 1. Extract JWT token from Authorization header
     * 2. Skip authentication for public endpoints
     * 3. Validate token signature and claims
     * 4. Create Spring Security authentication object
     * 5. Populate security context for downstream filters
     * 6. Continue filter chain or return 401 for invalid tokens
     * 
     * @param exchange ServerWebExchange containing request/response
     * @param chain WebFilterChain for continuing request processing
     * @return Mono<Void> representing async filter completion
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        
        // Skip authentication for public endpoints
        if (isPublicEndpoint(path)) {
            log.debug("Skipping JWT authentication for public endpoint: {}", path);
            return chain.filter(exchange);
        }

        // Extract JWT token from request
        String token = extractToken(exchange);
        
        if (token == null) {
            log.warn("Missing JWT token for protected endpoint: {}", path);
            return handleUnauthorized(exchange, "Missing authentication token");
        }

        // Validate token and create authentication
        return validateTokenAndAuthenticate(token)
                .flatMap(authentication -> {
                    // Set security context and continue
                    return chain.filter(exchange)
                            .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
                })
                .onErrorResume(InvalidTokenException.class, ex -> {
                    log.warn("JWT authentication failed for path {}: {}", path, ex.getMessage());
                    return handleUnauthorized(exchange, ex.getMessage());
                })
                .onErrorResume(Exception.class, ex -> {
                    log.error("Unexpected error during JWT authentication for path {}", path, ex);
                    return handleUnauthorized(exchange, "Authentication failed");
                });
    }

    /**
     * Extract JWT token from Authorization header.
     * 
     * Expected format: "Bearer <jwt-token>"
     * 
     * @param exchange ServerWebExchange containing the request
     * @return JWT token string without "Bearer " prefix, or null if not found
     */
    private String extractToken(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(AUTHORIZATION_HEADER);
        
        if (StringUtils.hasText(authHeader) && authHeader.startsWith(BEARER_PREFIX)) {
            String token = authHeader.substring(BEARER_PREFIX.length());
            log.debug("Extracted JWT token from Authorization header");
            return token;
        }
        
        return null;
    }

    /**
     * Validate JWT token and create Spring Security Authentication object.
     * 
     * @param token JWT token string
     * @return Mono<Authentication> with user details and authorities
     */
    private Mono<Authentication> validateTokenAndAuthenticate(String token) {
        return Mono.fromCallable(() -> {
            // Validate token and extract claims
            Claims claims = jwtValidator.validateToken(token);
            
            // Extract user details
            String userId = jwtValidator.extractUserId(claims);
            List<String> roles = jwtValidator.extractRoles(claims);
            List<String> permissions = jwtValidator.extractPermissions(claims);
            
            // Create authorities from roles and permissions
            List<SimpleGrantedAuthority> authorities = roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                    .collect(Collectors.toList());
            
            permissions.stream()
                    .map(SimpleGrantedAuthority::new)
                    .forEach(authorities::add);
            
            // Create authentication object
            UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(userId, null, authorities);
            
            // Add additional claims as details
            authentication.setDetails(claims);
            
            log.debug("JWT authentication successful for user: {} with roles: {}", userId, roles);
            return authentication;
        });
    }

    /**
     * Check if the request path is a public endpoint that doesn't require authentication.
     * 
     * Public endpoints include:
     * - Health check endpoints
     * - Authentication endpoints (login, register, etc.)
     * - CORS preflight requests
     * - Circuit breaker fallback endpoints
     * 
     * @param path Request path
     * @return true if endpoint is public
     */
    private boolean isPublicEndpoint(String path) {
        return path.startsWith("/actuator/health") ||
               path.startsWith("/actuator/info") ||
               path.startsWith("/actuator/prometheus") ||
               path.equals("/api/v1/auth/login") ||
               path.equals("/api/v1/auth/register") ||
               path.equals("/api/v1/auth/refresh") ||
               path.equals("/api/v1/auth/forgot-password") ||
               path.equals("/api/v1/auth/verify-otp") ||
               path.startsWith("/fallback/");
    }

    /**
     * Handle unauthorized requests by returning 401 status.
     * 
     * @param exchange ServerWebExchange for the request
     * @param message Error message for the response
     * @return Mono<Void> representing the error response
     */
    private Mono<Void> handleUnauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().add("Content-Type", "application/json");
        
        String errorResponse = String.format(
                "{\"success\":false,\"error\":{\"code\":\"UNAUTHORIZED\",\"message\":\"%s\"},\"timestamp\":\"%s\"}",
                message,
                java.time.Instant.now().toString()
        );
        
        org.springframework.core.io.buffer.DataBuffer buffer = 
                exchange.getResponse().bufferFactory().wrap(errorResponse.getBytes());
        
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}