package com.banking.orchestration.exception;

import com.banking.orchestration.dto.ApiResponse;
import io.micrometer.tracing.Tracer;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final Tracer tracer;

    public GlobalExceptionHandler(Tracer tracer) {
        this.tracer = tracer;
    }

    @ExceptionHandler(BudgetExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleBudgetExceeded(BudgetExceededException ex) {
        String traceId = getTraceId();
        logger.warn("Budget exceeded: {} - traceId: {}", ex.getMessage(), traceId);
        
        ApiResponse<Void> response = ApiResponse.error(
                ex.getErrorCode(),
                ex.getMessage(),
                traceId
        );
        return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(response);
    }

    @ExceptionHandler(QuotaExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleQuotaExceeded(QuotaExceededException ex) {
        String traceId = getTraceId();
        logger.warn("Quota exceeded: {} - traceId: {}", ex.getMessage(), traceId);
        
        ApiResponse<Void> response = ApiResponse.error(
                ex.getErrorCode(),
                ex.getMessage(),
                traceId
        );
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
    }

    @ExceptionHandler(ModelUnavailableException.class)
    public ResponseEntity<ApiResponse<Void>> handleModelUnavailable(ModelUnavailableException ex) {
        String traceId = getTraceId();
        logger.error("Model unavailable: {} - traceId: {}", ex.getMessage(), traceId, ex);
        
        ApiResponse<Void> response = ApiResponse.error(
                ex.getErrorCode(),
                ex.getMessage(),
                traceId
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @ExceptionHandler(AiOrchestrationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAiOrchestration(AiOrchestrationException ex) {
        String traceId = getTraceId();
        logger.error("AI orchestration error: {} - traceId: {}", ex.getMessage(), traceId, ex);
        
        ApiResponse<Void> response = ApiResponse.error(
                ex.getErrorCode(),
                ex.getMessage(),
                traceId
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationErrors(MethodArgumentNotValidException ex) {
        String traceId = getTraceId();
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());

        logger.warn("Validation failed: {} - traceId: {}", errors, traceId);
        
        ApiResponse<Void> response = ApiResponse.error(
                "VALIDATION_ERROR",
                "Request validation failed",
                errors,
                traceId
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException ex) {
        String traceId = getTraceId();
        List<String> errors = ex.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList());

        logger.warn("Constraint violation: {} - traceId: {}", errors, traceId);
        
        ApiResponse<Void> response = ApiResponse.error(
                "CONSTRAINT_VIOLATION",
                "Request constraint violation",
                errors,
                traceId
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthentication(AuthenticationException ex) {
        String traceId = getTraceId();
        logger.warn("Authentication failed: {} - traceId: {}", ex.getMessage(), traceId);
        
        ApiResponse<Void> response = ApiResponse.error(
                "AUTHENTICATION_FAILED",
                "Authentication failed",
                traceId
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        String traceId = getTraceId();
        logger.warn("Access denied: {} - traceId: {}", ex.getMessage(), traceId);
        
        ApiResponse<Void> response = ApiResponse.error(
                "ACCESS_DENIED",
                "Access denied",
                traceId
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        String traceId = getTraceId();
        logger.error("Unexpected error: {} - traceId: {}", ex.getMessage(), traceId, ex);
        
        ApiResponse<Void> response = ApiResponse.error(
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred",
                traceId
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    private String getTraceId() {
        if (tracer != null && tracer.currentSpan() != null && tracer.currentSpan().context() != null) {
            return tracer.currentSpan().context().traceId();
        }
        return java.util.UUID.randomUUID().toString();
    }
}
