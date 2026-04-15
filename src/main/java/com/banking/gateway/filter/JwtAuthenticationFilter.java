package com.banking.gateway.filter;

import com.banking.gateway.dto.ApiErrorResponse;
import com.banking.gateway.exception.InvalidTokenException;
import com.banking.gateway.util.JwtValidator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
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

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Global JWT authentication filter for the API Gateway.
 *
 * <p>This filter runs on EVERY incoming request before routing.
 * It is the first security boundary the platform enforces.
 *
 * <p>Processing logic:
 * <ol>
 *   <li>If the path is in the public allowlist → skip JWT validation and continue</li>
 *   <li>Extract the Bearer token from the Authorization header</li>
 *   <li>Validate the token (signature, expiry, issuer, audience)</li>
 *   <li>On success: enrich the request with X-User-Id, X-User-Roles headers
 *       for downstream service consumption</li>
 *   <li>On failure: short-circuit with 401 Unauthorized — never forward</li>
 * </ol>
 *
 * <p>SECURITY DESIGN NOTE:
 * Downstream services must trust these injected headers ONLY from the gateway.
 * In Kubernetes, NetworkPolicy ensures direct external access to downstream
 * services is blocked — all traffic must flow through the gateway.
 *
 * <p>Ref: https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#global-filters
 * Ref: https://docs.spring.io/spring-security/reference/reactive/index.html
 */
@Slf4j
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    /**
     * Paths that do not require JWT authentication.
     * These are exact prefix matches — evaluated in order.
     *
     * IMPORTANT: Never add business data endpoints here.
     * Anonymous AI insights are handled inside the AI service,
     * not at the gateway level, to allow proper response shaping.
     */
    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/refresh",
            "/api/v1/auth/forgot-password",
            "/api/v1/auth/otp/verify",
            "/api/v1/ai/chat/anonymous",      // anonymous AI — gateway allows, AI service anonymizes
            "/actuator/health",
            "/actuator/health/liveness",
            "/actuator/health/readiness",
            "/actuator/prometheus"
    );

    /**
     * Internal header names injected by the gateway for downstream services.
     * These headers are STRIPPED from inbound requests to prevent spoofing.
     */
    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_USER_ROLES = "X-User-Roles";
    public static final String HEADER_TRACE_ID = "X-Trace-Id";

    private final JwtValidator jwtValidator;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(
            final JwtValidator jwtValidator,
            final ObjectMapper objectMapper) {
        this.jwtValidator = jwtValidator;
        this.objectMapper = objectMapper;
    }

    /**
     * Filter execution order. Lower number = higher priority.
     * JWT auth must run BEFORE rate limiting (which needs userId from the token).
     * Set to -200 to run before Spring Cloud Gateway's built-in filters (order -1).
     */
    @Override
    public int getOrder() {
        return -200;
    }

    @Override
    public Mono<Void> filter(
            final ServerWebExchange exchange,
            final GatewayFilterChain chain) {

        final ServerHttpRequest request = exchange.getRequest();
        final String path = request.getURI().getPath();
        final String traceId = extractOrGenerateTraceId(request);

        // Always strip gateway-internal headers from inbound requests
        // to prevent clients from spoofing user identity.
        final ServerHttpRequest sanitizedRequest = request.mutate()
                .headers(headers -> {
                    headers.remove(HEADER_USER_ID);
                    headers.remove(HEADER_USER_ROLES);
                })
                .build();

        if (isPublicPath(path)) {
            log.debug("Public path — skipping JWT validation: path={} traceId={}", path, traceId);
            return chain.filter(exchange.mutate().request(sanitizedRequest).build());
        }

        final String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or malformed Authorization header: path={} traceId={}", path, traceId);
            return writeErrorResponse(
                    exchange,
                    HttpStatus.UNAUTHORIZED,
                    "MISSING_TOKEN",
                    "Authorization header with Bearer token is required",
                    traceId
            );
        }

        final String token = authHeader.substring(7);

        try {
            final Claims claims = jwtValidator.validateAndExtractClaims(token);
            final String userId = claims.getSubject();

            @SuppressWarnings("unchecked")
            List<String> roles = claims.get("roles", List.class);
            String rolesHeader = roles != null ? String.join(",", roles) : "";

            log.info("JWT validated: userId={} path={} traceId={}", userId, path, traceId);

            // Enrich the forwarded request with identity headers.
            // Downstream services read these to establish security context.
            final ServerHttpRequest enrichedRequest = sanitizedRequest.mutate()
                    .header(HEADER_USER_ID, userId)
                    .header(HEADER_USER_ROLES, rolesHeader)
                    .header(HEADER_TRACE_ID, traceId)
                    .build();

            return chain.filter(exchange.mutate().request(enrichedRequest).build());

        } catch (InvalidTokenException ex) {
            log.warn("JWT validation failed: {} path={} traceId={}", ex.getMessage(), path, traceId);
            return writeErrorResponse(
                    exchange,
                    HttpStatus.UNAUTHORIZED,
                    "INVALID_TOKEN",
                    ex.getMessage(),
                    traceId
            );
        }
    }

    /**
     * Writes a structured JSON error response and terminates the exchange.
     *
     * @param exchange    current server exchange
     * @param status      HTTP status to set
     * @param code        machine-readable error code
     * @param message     human-readable error message (safe for external consumers)
     * @param traceId     correlation ID for distributed tracing
     * @return completion signal
     */
    private Mono<Void> writeErrorResponse(
            final ServerWebExchange exchange,
            final HttpStatus status,
            final String code,
            final String message,
            final String traceId) {

        final ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        response.getHeaders().set(HEADER_TRACE_ID, traceId);

        final ApiErrorResponse errorResponse = ApiErrorResponse.of(code, message, traceId);

        try {
            final byte[] bytes = objectMapper.writeValueAsBytes(errorResponse);
            final DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException ex) {
            log.error("Failed to serialize error response: traceId={}", traceId, ex);
            final byte[] fallback = ("{\"success\":false,\"traceId\":\"" + traceId + "\"}").getBytes(StandardCharsets.UTF_8);
            final DataBuffer buffer = response.bufferFactory().wrap(fallback);
            return response.writeWith(Mono.just(buffer));
        }
    }

    /**
     * Determines if the request path matches a public (unauthenticated) endpoint.
     * Uses prefix matching — a path starting with any entry in {@code PUBLIC_PATHS} is public.
     *
     * @param path request URI path
     * @return true if authentication is not required
     */
    private boolean isPublicPath(final String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    /**
     * Extracts the W3C Trace Context traceparent header or falls back to
     * a request-scoped ID. In production, OpenTelemetry agent injects this.
     *
     * @param request incoming HTTP request
     * @return trace ID string for logging and response headers
     */
    private String extractOrGenerateTraceId(final ServerHttpRequest request) {
        String traceparent = request.getHeaders().getFirst("traceparent");
        if (traceparent != null && traceparent.length() > 32) {
            // W3C traceparent format: 00-{traceId}-{spanId}-{flags}
            String[] parts = traceparent.split("-");
            if (parts.length >= 2) {
                return parts[1];
            }
        }
        // Fallback: generate a simple hex trace ID for non-instrumented requests
        return Long.toHexString(System.currentTimeMillis()) +
               Long.toHexString(Thread.currentThread().threadId());
    }
}
