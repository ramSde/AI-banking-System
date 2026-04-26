package com.banking.gateway.filter;

import com.banking.gateway.exception.InvalidTokenException;
import com.banking.gateway.util.JwtValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * JWT Authentication Filter
 * 
 * Global filter that validates JWT tokens for all incoming requests.
 * Runs before routing to downstream services.
 * 
 * Authentication Flow:
 * 1. Extract Authorization header from request
 * 2. Validate Bearer token format
 * 3. Verify JWT signature using RS256 public key
 * 4. Validate token claims (issuer, audience, expiration)
 * 5. Extract user ID and roles from token
 * 6. Add user context headers to downstream request
 * 
 * Public Endpoints (skip authentication):
 * - /actuator/health, /actuator/info
 * - /api-docs, /swagger-ui
 * - /api/v1/auth/** (login, register, refresh, logout)
 * 
 * Authenticated Endpoints:
 * - All other /api/v1/** endpoints require valid JWT
 * 
 * Headers Added to Downstream Requests:
 * - X-User-Id: Extracted from JWT subject claim
 * - X-User-Roles: Comma-separated list of roles from JWT
 * - X-Trace-Id: Request trace ID for distributed tracing
 * 
 * Error Handling:
 * - Missing token: 401 Unauthorized
 * - Invalid token: 401 Unauthorized
 * - Expired token: 401 Unauthorized
 * 
 * @author Banking Platform Team
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtValidator jwtValidator;

    private static final List<String> PUBLIC_PATHS = List.of(
            "/actuator/health",
            "/actuator/info",
            "/api-docs",
            "/swagger-ui",
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/refresh",
            "/api/v1/auth/logout"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        if (isPublicPath(path)) {
            log.debug("Public path accessed: {}", path);
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header for path: {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);

        try {
            String userId = jwtValidator.validateTokenAndGetUserId(token);
            List<String> roles = jwtValidator.extractRoles(token);

            log.debug("JWT validated successfully for user: {} (roles: {})", userId, roles);

            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Id", userId)
                    .header("X-User-Roles", String.join(",", roles))
                    .header("X-Trace-Id", exchange.getRequest().getId())
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (InvalidTokenException e) {
            log.error("JWT validation failed for path {}: {}", path, e.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
