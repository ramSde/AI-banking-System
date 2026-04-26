package com.banking.rag.exception;

import com.banking.rag.dto.ApiResponse;
import io.micrometer.tracing.Tracer;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final Tracer tracer;

    private String getTraceId() {
        if (tracer.currentSpan() != null) {
            return tracer.currentSpan().context().traceId();
        }
        return "no-trace-id";
    }

    @ExceptionHandler(QueryNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleQueryNotFoundException(
            QueryNotFoundException ex, WebRequest request) {
        log.error("Query not found: {}", ex.getMessage());
        ApiResponse<Void> response = ApiResponse.error(ex.getErrorCode(), ex.getMessage(), getTraceId());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(VectorSearchException.class)
    public ResponseEntity<ApiResponse<Void>> handleVectorSearchException(
            VectorSearchException ex, WebRequest request) {
        log.error("Vector search error: {}", ex.getMessage(), ex);
        ApiResponse<Void> response = ApiResponse.error(ex.getErrorCode(), ex.getMessage(), getTraceId());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(RerankingException.class)
    public ResponseEntity<ApiResponse<Void>> handleRerankingException(
            RerankingException ex, WebRequest request) {
        log.error("Reranking error: {}", ex.getMessage(), ex);
        ApiResponse<Void> response = ApiResponse.error(ex.getErrorCode(), ex.getMessage(), getTraceId());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(RagException.class)
    public ResponseEntity<ApiResponse<Void>> handleRagException(
            RagException ex, WebRequest request) {
        log.error("RAG error: {}", ex.getMessage(), ex);
        ApiResponse<Void> response = ApiResponse.error(ex.getErrorCode(), ex.getMessage(), getTraceId());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());

        log.warn("Validation error: {}", errors);
        ApiResponse.ErrorDetails errorDetails = new ApiResponse.ErrorDetails(
                "VALIDATION_ERROR",
                "Request validation failed",
                errors
        );
        ApiResponse<Void> response = ApiResponse.error(errorDetails, getTraceId());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(
            ConstraintViolationException ex, WebRequest request) {
        List<String> errors = ex.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList());

        log.warn("Constraint violation: {}", errors);
        ApiResponse.ErrorDetails errorDetails = new ApiResponse.ErrorDetails(
                "VALIDATION_ERROR",
                "Request validation failed",
                errors
        );
        ApiResponse<Void> response = ApiResponse.error(errorDetails, getTraceId());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(
            AuthenticationException ex, WebRequest request) {
        log.error("Authentication error: {}", ex.getMessage());
        ApiResponse<Void> response = ApiResponse.error("AUTHENTICATION_ERROR", "Authentication failed", getTraceId());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {
        log.error("Access denied: {}", ex.getMessage());
        ApiResponse<Void> response = ApiResponse.error("ACCESS_DENIED", "Access denied", getTraceId());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGlobalException(
            Exception ex, WebRequest request) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        ApiResponse<Void> response = ApiResponse.error(
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred. Please try again later.",
                getTraceId()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
