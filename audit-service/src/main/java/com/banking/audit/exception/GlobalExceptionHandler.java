package com.banking.audit.exception;

import com.banking.audit.dto.ApiResponse;
import io.micrometer.tracing.Tracer;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final Tracer tracer;

    public GlobalExceptionHandler(Tracer tracer) {
        this.tracer = tracer;
    }

    @ExceptionHandler(AuditException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuditException(AuditException ex, WebRequest request) {
        String traceId = getTraceId();
        logger.error("Audit exception occurred: code={}, message={}, traceId={}", 
                ex.getCode(), ex.getMessage(), traceId, ex);

        ApiResponse<Void> response = ApiResponse.error(
                ex.getCode(),
                ex.getMessage(),
                traceId
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException ex,
            WebRequest request
    ) {
        String traceId = getTraceId();
        List<String> errors = new ArrayList<>();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add(error.getField() + ": " + error.getDefaultMessage());
        }

        logger.warn("Validation failed: errors={}, traceId={}", errors, traceId);

        ApiResponse<Void> response = ApiResponse.error(
                "VALIDATION_ERROR",
                "Validation failed",
                errors,
                traceId
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(
            ConstraintViolationException ex,
            WebRequest request
    ) {
        String traceId = getTraceId();
        List<String> errors = ex.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList());

        logger.warn("Constraint violation: errors={}, traceId={}", errors, traceId);

        ApiResponse<Void> response = ApiResponse.error(
                "CONSTRAINT_VIOLATION",
                "Constraint violation",
                errors,
                traceId
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentialsException(
            BadCredentialsException ex,
            WebRequest request
    ) {
        String traceId = getTraceId();
        logger.warn("Bad credentials: message={}, traceId={}", ex.getMessage(), traceId);

        ApiResponse<Void> response = ApiResponse.error(
                "INVALID_CREDENTIALS",
                "Invalid credentials",
                traceId
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(
            AccessDeniedException ex,
            WebRequest request
    ) {
        String traceId = getTraceId();
        logger.warn("Access denied: message={}, traceId={}", ex.getMessage(), traceId);

        ApiResponse<Void> response = ApiResponse.error(
                "ACCESS_DENIED",
                "Access denied",
                traceId
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(
            IllegalArgumentException ex,
            WebRequest request
    ) {
        String traceId = getTraceId();
        logger.warn("Illegal argument: message={}, traceId={}", ex.getMessage(), traceId);

        ApiResponse<Void> response = ApiResponse.error(
                "ILLEGAL_ARGUMENT",
                ex.getMessage(),
                traceId
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex, WebRequest request) {
        String traceId = getTraceId();
        logger.error("Unexpected error occurred: message={}, traceId={}", ex.getMessage(), traceId, ex);

        ApiResponse<Void> response = ApiResponse.error(
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred. Please try again later.",
                traceId
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    private String getTraceId() {
        if (tracer != null && tracer.currentSpan() != null && tracer.currentSpan().context() != null) {
            return tracer.currentSpan().context().traceId();
        }
        return "no-trace-id";
    }
}
