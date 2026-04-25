package com.banking.rag.exception;

import com.banking.rag.dto.ApiResponse;
import io.micrometer.tracing.Tracer;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Tracer tracer;

    public GlobalExceptionHandler(Tracer tracer) {
        this.tracer = tracer;
    }

    @ExceptionHandler(RetrievalException.class)
    public ResponseEntity<ApiResponse<Void>> handleRetrievalException(RetrievalException ex, WebRequest request) {
        log.error("Retrieval error: {}", ex.getMessage(), ex);
        String traceId = getTraceId();
        ApiResponse<Void> response = ApiResponse.error(
                ex.getErrorCode(),
                ex.getMessage(),
                traceId
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(RerankingException.class)
    public ResponseEntity<ApiResponse<Void>> handleRerankingException(RerankingException ex, WebRequest request) {
        log.error("Reranking error: {}", ex.getMessage(), ex);
        String traceId = getTraceId();
        ApiResponse<Void> response = ApiResponse.error(
                ex.getErrorCode(),
                ex.getMessage(),
                traceId
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(ContextAssemblyException.class)
    public ResponseEntity<ApiResponse<Void>> handleContextAssemblyException(ContextAssemblyException ex, WebRequest request) {
        log.error("Context assembly error: {}", ex.getMessage(), ex);
        String traceId = getTraceId();
        ApiResponse<Void> response = ApiResponse.error(
                ex.getErrorCode(),
                ex.getMessage(),
                traceId
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(RagException.class)
    public ResponseEntity<ApiResponse<Void>> handleRagException(RagException ex, WebRequest request) {
        log.error("RAG error: {}", ex.getMessage(), ex);
        String traceId = getTraceId();
        ApiResponse<Void> response = ApiResponse.error(
                ex.getErrorCode(),
                ex.getMessage(),
                traceId
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex, WebRequest request) {
        log.warn("Validation error: {}", ex.getMessage());
        String traceId = getTraceId();
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ApiResponse<Void> response = ApiResponse.error(
                "VALIDATION_ERROR",
                "Input validation failed",
                errors,
                traceId
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(ConstraintViolationException ex, WebRequest request) {
        log.warn("Constraint violation: {}", ex.getMessage());
        String traceId = getTraceId();
        
        Map<String, String> errors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        ConstraintViolation::getMessage
                ));

        ApiResponse<Void> response = ApiResponse.error(
                "VALIDATION_ERROR",
                "Constraint validation failed",
                errors,
                traceId
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentialsException(BadCredentialsException ex, WebRequest request) {
        log.warn("Authentication failed: {}", ex.getMessage());
        String traceId = getTraceId();
        ApiResponse<Void> response = ApiResponse.error(
                "AUTHENTICATION_FAILED",
                "Invalid credentials",
                traceId
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        log.warn("Access denied: {}", ex.getMessage());
        String traceId = getTraceId();
        ApiResponse<Void> response = ApiResponse.error(
                "ACCESS_DENIED",
                "You do not have permission to access this resource",
                traceId
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        log.warn("Illegal argument: {}", ex.getMessage());
        String traceId = getTraceId();
        ApiResponse<Void> response = ApiResponse.error(
                "INVALID_ARGUMENT",
                ex.getMessage(),
                traceId
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGlobalException(Exception ex, WebRequest request) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        String traceId = getTraceId();
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
