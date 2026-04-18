package com.banking.risk.exception;

import com.banking.risk.dto.ApiResponse;
import com.banking.risk.dto.ApiResponse.ErrorDetails;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Global exception handler for the risk service.
 * Provides consistent error responses across all endpoints.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle validation errors from @Valid annotations.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException ex,
            WebRequest request
    ) {
        List<String> details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        ErrorDetails error = ErrorDetails.builder()
                .code("VALIDATION_ERROR")
                .message("Input validation failed")
                .details(details)
                .build();

        logger.warn("Validation error: {}", details);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(error, getTraceId()));
    }

    /**
     * Handle constraint violation exceptions.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(
            ConstraintViolationException ex,
            WebRequest request
    ) {
        List<String> details = ex.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList());

        ErrorDetails error = ErrorDetails.builder()
                .code("CONSTRAINT_VIOLATION")
                .message("Constraint violation")
                .details(details)
                .build();

        logger.warn("Constraint violation: {}", details);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(error, getTraceId()));
    }

    /**
     * Handle risk assessment not found exceptions.
     */
    @ExceptionHandler(RiskAssessmentNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleRiskAssessmentNotFoundException(
            RiskAssessmentNotFoundException ex,
            WebRequest request
    ) {
        ErrorDetails error = ErrorDetails.builder()
                .code("RISK_ASSESSMENT_NOT_FOUND")
                .message(ex.getMessage())
                .details(new ArrayList<>())
                .build();

        logger.warn("Risk assessment not found: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(error, getTraceId()));
    }

    /**
     * Handle risk rule not found exceptions.
     */
    @ExceptionHandler(RiskRuleNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleRiskRuleNotFoundException(
            RiskRuleNotFoundException ex,
            WebRequest request
    ) {
        ErrorDetails error = ErrorDetails.builder()
                .code("RISK_RULE_NOT_FOUND")
                .message(ex.getMessage())
                .details(new ArrayList<>())
                .build();

        logger.warn("Risk rule not found: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(error, getTraceId()));
    }

    /**
     * Handle duplicate risk rule exceptions.
     */
    @ExceptionHandler(DuplicateRiskRuleException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateRiskRuleException(
            DuplicateRiskRuleException ex,
            WebRequest request
    ) {
        ErrorDetails error = ErrorDetails.builder()
                .code("DUPLICATE_RISK_RULE")
                .message(ex.getMessage())
                .details(new ArrayList<>())
                .build();

        logger.warn("Duplicate risk rule: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(error, getTraceId()));
    }

    /**
     * Handle risk assessment exceptions.
     */
    @ExceptionHandler(RiskAssessmentException.class)
    public ResponseEntity<ApiResponse<Void>> handleRiskAssessmentException(
            RiskAssessmentException ex,
            WebRequest request
    ) {
        ErrorDetails error = ErrorDetails.builder()
                .code("RISK_ASSESSMENT_ERROR")
                .message(ex.getMessage())
                .details(new ArrayList<>())
                .build();

        logger.error("Risk assessment error: {}", ex.getMessage(), ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(error, getTraceId()));
    }

    /**
     * Handle authentication exceptions.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(
            AuthenticationException ex,
            WebRequest request
    ) {
        ErrorDetails error = ErrorDetails.builder()
                .code("AUTHENTICATION_ERROR")
                .message("Authentication failed")
                .details(new ArrayList<>())
                .build();

        logger.warn("Authentication error: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(error, getTraceId()));
    }

    /**
     * Handle access denied exceptions.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(
            AccessDeniedException ex,
            WebRequest request
    ) {
        ErrorDetails error = ErrorDetails.builder()
                .code("ACCESS_DENIED")
                .message("Access denied")
                .details(new ArrayList<>())
                .build();

        logger.warn("Access denied: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(error, getTraceId()));
    }

    /**
     * Handle all other exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(
            Exception ex,
            WebRequest request
    ) {
        ErrorDetails error = ErrorDetails.builder()
                .code("INTERNAL_SERVER_ERROR")
                .message("An unexpected error occurred")
                .details(new ArrayList<>())
                .build();

        logger.error("Unexpected error: {}", ex.getMessage(), ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(error, getTraceId()));
    }

    /**
     * Get trace ID from MDC or generate a new one.
     */
    private String getTraceId() {
        String traceId = MDC.get("traceId");
        return traceId != null ? traceId : java.util.UUID.randomUUID().toString();
    }
}
