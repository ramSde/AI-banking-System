package com.banking.gateway.filter;

import com.banking.gateway.dto.ApiErrorResponse;
import com.banking.gateway.exception.InvalidTokenException;
import com.banking.gateway.util.JwtValidator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * JWT Authentication Filter for Spring Cloud Gateway.
 * 
 * Validates JWT tokens on all incoming requests except public endpoints.
 * Extracts user information from valid tokens and adds to request headers
 * for downstream services.
 * 
 * Security features:
 * - RSA signature validation
 * - Token expiration checking
 * - Issuer and audience validation
 * - Request context enrichment with user data
 * - Comprehensive error handling with structured responses
 * 
 * Public endpoints (no authentication required):
 * - /api/v1/auth/** (login, register, password reset)
 * - /actuator/** (health checks, metrics)
 * - OPTIONS requests (CORS preflight)
 * 
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtValidator jwtValidator;
    private final ObjectMapper objectMapper;

    /**
     * Public endpoints that don't require authentication
     */
    private static final List<String> PUBLIC_PATHS = List.of(
        "/api/v1/auth/",
        "/actuator/",
        "/fallback/"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        String method = request.getMethod().name();

        // Skip authentication for public endpoints and OPTIONS requests
        if (isPublicPath(path) || "OPTIONS".equals(method)) {
            log.debug("Skipping authentication for public path: {} {}", method, path);
            return chain.filter(exchange);
        }

        // Extract JWT token from Authorization header
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header for path: {}", path);
            return handleAuthenticationError(exchange, "Missing or invalid Authorization header", 
                                           HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7); // Remove "Bearer " prefix
        
        try {
            // Validate JWT token
            Claims claims = jwtValidator.validateToken(token);
            
            // Extract user information
            String userId = claims.getSubject();
            String email = claims.get("email", String.class);
            String roles = claims.get("roles", String.class);
            
            // Enrich request with user context for downstream services
            ServerHttpRequest enrichedRequest = request.mutate()
                .header("X-User-ID", userId)
                .header("X-User-Email", email != null ? email : "")
                .header("X-User-Roles", roles != null ? roles : "")
                .header("X-Token-Issued-At", String.valueOf(claims.getIssuedAt().getTime()))
                .header("X-Token-Expires-At", String.valueOf(claims.getExpiration().getTime()))
                .build();

            // Add user ID to MDC for logging context
            exchange.getAttributes().put("userId", userId);
            
            log.info("Authentication successful for user: {} on path: {}", 
                    maskUserId(userId), path);
            
            return chain.filter(exchange.mutate().request(enrichedRequest).build());
            
        } catch (InvalidTokenException e) {
            log.warn("JWT authentication failed for path {}: {}", path, e.getMessage());
            return handleAuthenticationError(exchange, e.getMessage(), HttpStatus.UNAUTHORIZED);
            
        } catch (Exception e) {
            log.error("Unexpected error during JWT authentication for path: " + path, e);
            return handleAuthenticationError(exchange, "Authentication failed", 
                                           HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Checks if the request path is public (no authentication required).
     * 
     * @param path Request path
     * @return true if path is public, false otherwise
     */
    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    /**
     * Handles authentication errors with structured JSON response.
     * 
     * @param exchange Server web exchange
     * @param message Error message
     * @param status HTTP status code
     * @return Mono<Void> representing the error response
     */
    private Mono<Void> handleAuthenticationError(ServerWebExchange exchange, 
                                               String message, 
                                               HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
            .success(false)
            .error(ApiErrorResponse.ErrorDetails.builder()
                .code(status == HttpStatus.UNAUTHORIZED ? "AUTHENTICATION_FAILED" : "INTERNAL_ERROR")
                .message(message)
                .build())
            .traceId(UUID.randomUUID().toString())
            .timestamp(Instant.now())
            .build();

        try {
            String errorJson = objectMapper.writeValueAsString(errorResponse);
            DataBuffer buffer = response.bufferFactory().wrap(errorJson.getBytes());
            return response.writeWith(Mono.just(buffer));
            
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize error response", e);
            return response.setComplete();
        }
    }

    /**
     * Masks user ID for logging (privacy protection).
     * 
     * @param userId User ID to mask
     * @return Masked user ID for safe logging
     */
    private String maskUserId(String userId) {
        if (userId == null || userId.length() <= 8) {
            return "****";
        }
        return userId.substring(0, 4) + "****" + userId.substring(userId.length() - 4);
    }

    @Override
    public int getOrder() {
        return -100; // Execute before rate limiting filter
    }
}