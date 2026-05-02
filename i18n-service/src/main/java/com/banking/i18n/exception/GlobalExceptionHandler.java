package com.banking.i18n.exception;

import com.banking.i18n.dto.ApiResponse;
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
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(TranslationNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleTranslationNotFound(
            TranslationNotFoundException ex, WebRequest request) {
        
        String traceId = UUID.randomUUID().toString();
        logger.error("Translation not found: {}, traceId: {}", ex.getMessage(), traceId);
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(
                        false,
                        null,
                        new ApiResponse.ErrorDetails(
                                "TRANSLATION_NOT_FOUND",
                                ex.getMessage(),
                                null
                        ),
                        traceId,
                        Instant.now()
                ));
    }

    @ExceptionHandler(UnsupportedLocaleException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnsupportedLocale(
            UnsupportedLocaleException ex, WebRequest request) {
        
        String traceId = UUID.randomUUID().toString();
        logger.error("Unsupported locale: {}, traceId: {}", ex.getMessage(), traceId);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(
                        false,
                        null,
                        new ApiResponse.ErrorDetails(
                                "UNSUPPORTED_LOCALE",
                                ex.getMessage(),
                                null
                        ),
                        traceId,
                        Instant.now()
                ));
    }

    @ExceptionHandler(I18nException.class)
    public ResponseEntity<ApiResponse<Void>> handleI18nException(
            I18nException ex, WebRequest request) {
        
        String traceId = UUID.randomUUID().toString();
        logger.error("I18n exception: {}, traceId: {}", ex.getMessage(), traceId, ex);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(
                        false,
                        null,
                        new ApiResponse.ErrorDetails(
                                "I18N_ERROR",
                                ex.getMessage(),
                                null
                        ),
                        traceId,
                        Instant.now()
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        String traceId = UUID.randomUUID().toString();
        
        List<String> errors = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> {
                    String fieldName = ((FieldError) error).getField();
                    String errorMessage = error.getDefaultMessage();
                    return fieldName + ": " + errorMessage;
                })
                .collect(Collectors.toList());
        
        logger.error("Validation failed: {}, traceId: {}", errors, traceId);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(
                        false,
                        null,
                        new ApiResponse.ErrorDetails(
                                "VALIDATION_FAILED",
                                "Request validation failed",
                                errors
                        ),
                        traceId,
                        Instant.now()
                ));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(
            AuthenticationException ex, WebRequest request) {
        
        String traceId = UUID.randomUUID().toString();
        logger.error("Authentication failed: {}, traceId: {}", ex.getMessage(), traceId);
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>(
                        false,
                        null,
                        new ApiResponse.ErrorDetails(
                                "AUTHENTICATION_FAILED",
                                "Authentication failed",
                                null
                        ),
                        traceId,
                        Instant.now()
                ));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {
        
        String traceId = UUID.randomUUID().toString();
        logger.error("Access denied: {}, traceId: {}", ex.getMessage(), traceId);
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiResponse<>(
                        false,
                        null,
                        new ApiResponse.ErrorDetails(
                                "ACCESS_DENIED",
                                "Access denied",
                                null
                        ),
                        traceId,
                        Instant.now()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(
            Exception ex, WebRequest request) {
        
        String traceId = UUID.randomUUID().toString();
        logger.error("Unexpected error: {}, traceId: {}", ex.getMessage(), traceId, ex);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(
                        false,
                        null,
                        new ApiResponse.ErrorDetails(
                                "INTERNAL_SERVER_ERROR",
                                "An unexpected error occurred",
                                null
                        ),
                        traceId,
                        Instant.now()
                ));
    }
}
