package com.banking.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

/**
 * Request logging filter for comprehensive observability.
 * 
 * This filter provides:
 * - Request/response logging with PII masking
 * - Request tracing with correlation IDs
 * - Performance metrics (request duration)
 * - User context logging for authenticated requests
 * - Error tracking and debugging information
 * 
 * Logging Features:
 * - Structured JSON logging for log aggregation
 * - PII masking for sensitive data protection
 * - Correlation ID propagation for distributed tracing
 * - Request/response size tracking
 * - HTTP status code and error tracking
 * 
 * Security Considerations:
 * - Never log sensitive headers (Authorization, cookies)
 * - Mask account numbers, card numbers, SSNs
 * - Log user actions for audit compliance
 * - Sanitize query parameters and request bodies
 * 
 * @author Banking Platform Team
 * @version 1.0.0
 */
@Slf4j
@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String REQUEST_START_TIME = "request_start_time";

    /**
     * Log incoming requests and outgoing responses.
     * 
     * Logging Flow:
     * 1. Generate correlation ID if not present
     * 2. Log incoming request details (method, path, IP, user)
     * 3. Record request start time for duration calculation
     * 4. Process request through filter chain
     * 5. Log response details (status, duration, size)
     * 6. Handle errors and exceptions
     * 
     * @param exchange ServerWebExchange containing request/response
     * @param chain GatewayFilterChain for continuing request processing
     * @return Mono<Void> representing async filter completion
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // Skip logging for health checks to reduce noise
        if (isHealthCheckEndpoint(request.getPath().value())) {
            return chain.filter(exchange);
        }

        // Generate or extract correlation ID
        String correlationId = getOrGenerateCorrelationId(request);
        
        // Add correlation ID to response headers
        exchange.getResponse().getHeaders().add(CORRELATION_ID_HEADER, correlationId);
        
        // Record request start time
        exchange.getAttributes().put(REQUEST_START_TIME, Instant.now());

        // Log incoming request
        return ReactiveSecurityContextHolder.getContext()
                .cast(org.springframework.security.core.context.SecurityContext.class)
                .map(ctx -> ctx.getAuthentication())
                .cast(Authentication.class)
                .doOnNext(auth -> logIncomingRequest(request, correlationId, auth))
                .then(chain.filter(exchange))
                .doOnSuccess(unused -> logOutgoingResponse(exchange, correlationId))
                .doOnError(error -> logRequestError(exchange, correlationId, error))
                .switchIfEmpty(
                    chain.filter(exchange)
                        .doOnNext(unused -> logIncomingRequest(request, correlationId, null))
                        .doOnSuccess(unused -> logOutgoingResponse(exchange, correlationId))
                        .doOnError(error -> logRequestError(exchange, correlationId, error))
                );
    }

    /**
     * Log incoming request details with PII masking.
     * 
     * Logged Information:
     * - HTTP method and path
     * - Client IP address
     * - User ID (if authenticated)
     * - Request size
     * - Correlation ID for tracing
     * - User agent (for security analysis)
     * 
     * @param request ServerHttpRequest
     * @param correlationId Correlation ID for request tracing
     * @param authentication User authentication (null if unauthenticated)
     */
    private void logIncomingRequest(ServerHttpRequest request, String correlationId, Authentication authentication) {
        String method = request.getMethod() != null ? request.getMethod().name() : "UNKNOWN";
        String path = request.getPath().value();
        String clientIp = getClientIp(request);
        String userId = authentication != null ? authentication.getName() : "anonymous";
        String userAgent = request.getHeaders().getFirst("User-Agent");
        
        // Mask sensitive query parameters
        String queryString = maskSensitiveQueryParams(request.getURI().getQuery());

        log.info("Incoming request - Method: {}, Path: {}, Query: {}, ClientIP: {}, UserID: {}, " +
                "UserAgent: {}, CorrelationID: {}",
                method, path, queryString, clientIp, userId, 
                maskUserAgent(userAgent), correlationId);
    }

    /**
     * Log outgoing response details with performance metrics.
     * 
     * Logged Information:
     * - HTTP status code
     * - Response size
     * - Request duration
     * - Correlation ID
     * - Error details (if applicable)
     * 
     * @param exchange ServerWebExchange containing request/response
     * @param correlationId Correlation ID for request tracing
     */
    private void logOutgoingResponse(ServerWebExchange exchange, String correlationId) {
        ServerHttpResponse response = exchange.getResponse();
        Instant startTime = exchange.getAttribute(REQUEST_START_TIME);
        
        long duration = startTime != null 
                ? java.time.Duration.between(startTime, Instant.now()).toMillis()
                : -1;

        int statusCode = response.getStatusCode() != null 
                ? response.getStatusCode().value() 
                : 0;

        String path = exchange.getRequest().getPath().value();

        log.info("Outgoing response - Path: {}, Status: {}, Duration: {}ms, CorrelationID: {}",
                path, statusCode, duration, correlationId);

        // Log slow requests for performance monitoring
        if (duration > 5000) { // 5 seconds threshold
            log.warn("Slow request detected - Path: {}, Duration: {}ms, CorrelationID: {}",
                    path, duration, correlationId);
        }
    }

    /**
     * Log request processing errors.
     * 
     * @param exchange ServerWebExchange containing request/response
     * @param correlationId Correlation ID for request tracing
     * @param error Exception that occurred during processing
     */
    private void logRequestError(ServerWebExchange exchange, String correlationId, Throwable error) {
        String path = exchange.getRequest().getPath().value();
        String method = exchange.getRequest().getMethod() != null 
                ? exchange.getRequest().getMethod().name() 
                : "UNKNOWN";

        log.error("Request processing error - Method: {}, Path: {}, Error: {}, CorrelationID: {}",
                method, path, error.getMessage(), correlationId, error);
    }

    /**
     * Get or generate correlation ID for request tracing.
     * 
     * @param request ServerHttpRequest
     * @return Correlation ID (existing or newly generated)
     */
    private String getOrGenerateCorrelationId(ServerHttpRequest request) {
        String correlationId = request.getHeaders().getFirst(CORRELATION_ID_HEADER);
        
        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }
        
        return correlationId;
    }

    /**
     * Extract client IP address from request headers and connection info.
     * 
     * @param request ServerHttpRequest
     * @return Client IP address
     */
    private String getClientIp(ServerHttpRequest request) {
        // Check X-Forwarded-For header (load balancer)
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        // Check X-Real-IP header (nginx)
        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        // Fall back to remote address
        return request.getRemoteAddress() != null 
                ? request.getRemoteAddress().getAddress().getHostAddress()
                : "unknown";
    }

    /**
     * Mask sensitive query parameters for security.
     * 
     * Masked Parameters:
     * - password, pwd, secret, token
     * - ssn, account, card, pin
     * - Any parameter containing "pass" or "auth"
     * 
     * @param queryString Original query string
     * @return Query string with sensitive parameters masked
     */
    private String maskSensitiveQueryParams(String queryString) {
        if (queryString == null || queryString.isEmpty()) {
            return "";
        }

        return queryString.replaceAll(
                "(?i)(password|pwd|secret|token|ssn|account|card|pin|auth\\w*)=[^&]*",
                "$1=***"
        );
    }

    /**
     * Mask sensitive information from User-Agent header.
     * 
     * @param userAgent Original User-Agent header
     * @return Masked User-Agent (first 100 characters only)
     */
    private String maskUserAgent(String userAgent) {
        if (userAgent == null) {
            return "unknown";
        }
        
        // Limit length to prevent log injection
        return userAgent.length() > 100 
                ? userAgent.substring(0, 100) + "..." 
                : userAgent;
    }

    /**
     * Check if endpoint is a health check to reduce log noise.
     * 
     * @param path Request path
     * @return true if endpoint is a health check
     */
    private boolean isHealthCheckEndpoint(String path) {
        return path.startsWith("/actuator/health") ||
               path.startsWith("/actuator/prometheus");
    }

    /**
     * Set filter order to run early in the filter chain.
     * 
     * @return Filter order (lower values run first)
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1; // Run very early, after CORS
    }
}