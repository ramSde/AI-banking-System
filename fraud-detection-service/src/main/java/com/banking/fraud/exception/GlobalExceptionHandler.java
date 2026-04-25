package com.banking.fraud.exception;

import com.banking.fraud.dto.ApiResponse;
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

import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler
 * 
 * Handles all exceptions and returns standardized error responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle fraud rule not found exception
     * 
     * @param ex Exception
     * @return Error response
     */
    @ExceptionHandler(FraudRuleNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleFraudRuleNotFoundException(FraudRuleNotFoundException ex) {
        log.error("Fraud rule not found: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("FRAUD_RULE_NOT_FOUND", ex.getMessage()));
    }

    /**
     * Handle invalid rule exception
     * 
     * @param ex Exception
     * @return Error response
     */
    @ExceptionHandler(InvalidRuleException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidRuleException(InvalidRuleException ex) {
        log.error("Invalid rule: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("INVALID_RULE", ex.getMessage()));
    }

    /**
     * Handle general fraud exception
     * 
     * @param ex Exception
     * @return Error response
     */
    @ExceptionHandler(FraudException.class)
    public ResponseEntity<ApiResponse<Void>> handleFraudException(FraudException ex) {
        log.error("Fraud exception: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("FRAUD_ERROR", ex.getMessage()));
    }

    /**
     * Handle validation exceptions
     * 
     * @param ex Exception
     * @return Error response
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.error("Validation failed: {}", errors);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("VALIDATION_ERROR", "Validation failed", errors));
    }

    /**
     * Handle authentication exceptions
     * 
     * @param ex Exception
     * @return Error response
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException ex) {
        log.error("Authentication failed: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("AUTHENTICATION_FAILED", "Authentication failed"));
    }

    /**
     * Handle access denied exceptions
     * 
     * @param ex Exception
     * @return Error response
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ex) {
        log.error("Access denied: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("ACCESS_DENIED", "Access denied"));
    }

    /**
     * Handle all other exceptions
     * 
     * @param ex Exception
     * @return Error response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("INTERNAL_ERROR", "An unexpected error occurred"));
    }
}
