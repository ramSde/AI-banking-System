package com.banking.gateway.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

/**
 * Request and response logging filter with PII masking.
 * 
 * Provides comprehensive request/response logging for:
 * - Security audit trails
 * - Performance monitoring
 * - Debugging and troubleshooting
 * - Compliance requirements
 * 
 * Security features:
 * - PII masking in headers and URLs
 * - Sensitive header filtering (Authorization, etc.)
 * - Request correlation ID generation
 * - Structured logging with MDC context
 * 
 * Performance considerations:
 * - Minimal overhead on request processing
 * - Async logging to avoid blocking
 * - Configurable log levels per environment
 * 
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String TRACE_ID_HEADER = "X-Trace-ID";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // Generate request ID if not present
        String requestId = request.getHeaders().getFirst(REQUEST_ID_HEADER);
        if (requestId == null) {
            requestId = UUID.randomUUID().toString();
        }
        
        // Set up MDC context for structured logging
        MDC.put("requestId", requestId);
        MDC.put("method", request.getMethod().name());
        MDC.put("path", request.getURI().getPath());
        MDC.put("clientIp", getClientIpAddress(request));
        
        // Extract user ID from exchange attributes (set by JWT filter)
        String userId = (String) exchange.getAttributes().get("userId");
        if (userId != null) {
            MDC.put("userId", userId);
        }

        // Log incoming request
        logIncomingRequest(request, requestId);
        
        // Record start time for performance measurement
        Instant startTime = Instant.now();
        exchange.getAttributes().put("startTime", startTime);
        
        // Add request ID to response headers
        ServerHttpRequest enrichedRequest = request.mutate()
            .header(REQUEST_ID_HEADER, requestId)
            .build();
        
        return chain.filter(exchange.mutate().request(enrichedRequest).build())
            .doOnSuccess(aVoid -> logOutgoingResponse(exchange, requestId, startTime))
            .doOnError(throwable -> logErrorResponse(exchange, requestId, startTime, throwable))
            .doFinally(signalType -> {
                // Clean up MDC context
                MDC.clear();
            });
    }

    /**
     * Logs incoming request with PII masking.
     * 
     * @param request HTTP request
     * @param requestId Request correlation ID
     */
    private void logIncomingRequest(ServerHttpRequest request, String requestId) {
        String method = request.getMethod().name();
        String path = request.getURI().getPath();
        String query = request.getURI().getQuery();
        String userAgent = request.getHeaders().getFirst(HttpHeaders.USER_AGENT);
        String clientIp = getClientIpAddress(request);
        
        // Mask sensitive query parameters
        String maskedQuery = maskSensitiveQueryParams(query);
        
        log.info("Incoming request: {} {} {} - IP: {}, User-Agent: {}, RequestId: {}",
                method, 
                path, 
                maskedQuery != null ? "?" + maskedQuery : "",
                maskIpAddress(clientIp),
                maskUserAgent(userAgent),
                requestId);
        
        // Log selected headers (excluding sensitive ones)
        logSafeHeaders(request.getHeaders(), "Request");
    }

    /**
     * Logs outgoing response with performance metrics.
     * 
     * @param exchange Server web exchange
     * @param requestId Request correlation ID
     * @param startTime Request start time
     */
    private void logOutgoingResponse(ServerWebExchange exchange, String requestId, Instant startTime) {
        ServerHttpResponse response = exchange.getResponse();
        long duration = Instant.now().toEpochMilli() - startTime.toEpochMilli();
        
        log.info("Outgoing response: {} - Duration: {}ms, RequestId: {}",
                response.getStatusCode(),
                duration,
                requestId);
        
        // Log performance warning for slow requests
        if (duration > 5000) { // 5 seconds
            log.warn("Slow request detected: {}ms for RequestId: {}", duration, requestId);
        }
    }

    /**
     * Logs error response with exception details.
     * 
     * @param exchange Server web exchange
     * @param requestId Request correlation ID
     * @param startTime Request start time
     * @param throwable Exception that occurred
     */
    private void logErrorResponse(ServerWebExchange exchange, String requestId, Instant startTime, Throwable throwable) {
        long duration = Instant.now().toEpochMilli() - startTime.toEpochMilli();
        
        log.error("Error response: Duration: {}ms, RequestId: {}, Error: {}",
                duration,
                requestId,
                throwable.getMessage(),
                throwable);
    }

    /**
     * Logs safe HTTP headers (excludes sensitive headers).
     * 
     * @param headers HTTP headers
     * @param type Request or Response
     */
    private void logSafeHeaders(HttpHeaders headers, String type) {
        if (log.isDebugEnabled()) {
            headers.forEach((name, values) -> {
                if (isSafeHeader(name)) {
                    log.debug("{} Header: {} = {}", type, name, String.join(", ", values));
                }
            });
        }
    }

    /**
     * Checks if header is safe to log (not sensitive).
     * 
     * @param headerName Header name
     * @return true if safe to log, false otherwise
     */
    private boolean isSafeHeader(String headerName) {
        String lowerName = headerName.toLowerCase();
        return !lowerName.contains("authorization") &&
               !lowerName.contains("cookie") &&
               !lowerName.contains("token") &&
               !lowerName.contains("password") &&
               !lowerName.contains("secret");
    }

    /**
     * Masks sensitive query parameters.
     * 
     * @param query Query string
     * @return Masked query string
     */
    private String maskSensitiveQueryParams(String query) {
        if (query == null) {
            return null;
        }
        
        // Mask common sensitive parameters
        return query.replaceAll("(?i)(password|token|secret|key)=[^&]*", "$1=***")
                   .replaceAll("(?i)(ssn|account)=[^&]*", "$1=***");
    }

    /**
     * Extracts client IP address from request.
     * 
     * @param request HTTP request
     * @return Client IP address
     */
    private String getClientIpAddress(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddress() != null 
            ? request.getRemoteAddress().getAddress().getHostAddress()
            : "unknown";
    }

    /**
     * Masks IP address for logging (privacy protection).
     * 
     * @param ip IP address
     * @return Masked IP address
     */
    private String maskIpAddress(String ip) {
        if (ip == null || ip.equals("unknown")) {
            return "unknown";
        }
        String[] parts = ip.split("\\.");
        if (parts.length == 4) {
            return parts[0] + "." + parts[1] + ".***." + parts[3];
        }
        return "***";
    }

    /**
     * Masks user agent for logging (privacy protection).
     * 
     * @param userAgent User agent string
     * @return Masked user agent
     */
    private String maskUserAgent(String userAgent) {
        if (userAgent == null) {
            return "unknown";
        }
        // Keep first 50 characters, mask the rest
        if (userAgent.length() > 50) {
            return userAgent.substring(0, 50) + "...";
        }
        return userAgent;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE; // Execute first to capture all requests
    }
}