package com.banking.notification.exception;

import com.banking.notification.dto.ApiResponse;
import io.micrometer.tracing.Tracer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private final Tracer tracer;

    public GlobalExceptionHandler(Tracer tracer) {
        this.tracer = tracer;
    }

    @ExceptionHandler(TemplateNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleTemplateNotFoundException(TemplateNotFoundException ex) {
        UUID traceId = getTraceId();
        log.error("Template not found: {} - traceId: {}", ex.getMessage(), traceId);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("TEMPLATE_NOT_FOUND", ex.getMessage(), traceId));
    }

    @ExceptionHandler(NotificationDeliveryException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotificationDeliveryException(NotificationDeliveryException ex) {
        UUID traceId = getTraceId();
        log.error("Notification delivery failed: {} - traceId: {}", ex.getMessage(), traceId, ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("NOTIFICATION_DELIVERY_FAILED", ex.getMessage(), traceId));
    }

    @ExceptionHandler(NotificationException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotificationException(NotificationException ex) {
        UUID traceId = getTraceId();
        log.error("Notification error: {} - traceId: {}", ex.getMessage(), traceId, ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("NOTIFICATION_ERROR", ex.getMessage(), traceId));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        UUID traceId = getTraceId();
        List<String> errors = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> {
                    String fieldName = ((FieldError) error).getField();
                    String errorMessage = error.getDefaultMessage();
                    return fieldName + ": " + errorMessage;
                })
                .collect(Collectors.toList());

        log.warn("Validation failed: {} - traceId: {}", errors, traceId);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("VALIDATION_ERROR", "Validation failed", errors, traceId));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException ex) {
        UUID traceId = getTraceId();
        log.warn("Authentication failed: {} - traceId: {}", ex.getMessage(), traceId);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("AUTHENTICATION_FAILED", "Authentication failed", traceId));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ex) {
        UUID traceId = getTraceId();
        log.warn("Access denied: {} - traceId: {}", ex.getMessage(), traceId);
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("ACCESS_DENIED", "Access denied", traceId));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        UUID traceId = getTraceId();
        log.error("Unexpected error: {} - traceId: {}", ex.getMessage(), traceId, ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("INTERNAL_SERVER_ERROR", "An unexpected error occurred", traceId));
    }

    private UUID getTraceId() {
        try {
            if (tracer != null && tracer.currentSpan() != null && tracer.currentSpan().context() != null) {
                String traceIdHex = tracer.currentSpan().context().traceId();
                return UUID.fromString(
                        traceIdHex.substring(0, 8) + "-" +
                                traceIdHex.substring(8, 12) + "-" +
                                traceIdHex.substring(12, 16) + "-" +
                                traceIdHex.substring(16, 20) + "-" +
                                traceIdHex.substring(20, 32)
                );
            }
        } catch (Exception e) {
            log.debug("Could not extract trace ID from tracer", e);
        }
        return UUID.randomUUID();
    }
}
