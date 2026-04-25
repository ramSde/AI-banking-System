package com.banking.account.exception;

import com.banking.account.dto.ApiResponse;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Global Exception Handler
 * 
 * Centralized exception handling for all controllers.
 * Returns consistent error responses across the API.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccountNotFoundException(
            AccountNotFoundException ex,
            WebRequest request
    ) {
        String traceId = UUID.randomUUID().toString();
        log.error("Account not found - TraceId: {}, Message: {}", traceId, ex.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error("ACCOUNT_NOT_FOUND", ex.getMessage(), traceId);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(AccountAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccountAlreadyExistsException(
            AccountAlreadyExistsException ex,
            WebRequest request
    ) {
        String traceId = UUID.randomUUID().toString();
        log.error("Account already exists - TraceId: {}, Message: {}", traceId, ex.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error("ACCOUNT_ALREADY_EXISTS", ex.getMessage(), traceId);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ApiResponse<Void>> handleInsufficientBalanceException(
            InsufficientBalanceException ex,
            WebRequest request
    ) {
        String traceId = UUID.randomUUID().toString();
        log.error("Insufficient balance - TraceId: {}, Message: {}", traceId, ex.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error("INSUFFICIENT_BALANCE", ex.getMessage(), traceId);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(AccountClosedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccountClosedException(
            AccountClosedException ex,
            WebRequest request
    ) {
        String traceId = UUID.randomUUID().toString();
        log.error("Account closed - TraceId: {}, Message: {}", traceId, ex.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error("ACCOUNT_CLOSED", ex.getMessage(), traceId);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(AccountFrozenException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccountFrozenException(
            AccountFrozenException ex,
            WebRequest request
    ) {
        String traceId = UUID.randomUUID().toString();
        log.error("Account frozen - TraceId: {}, Message: {}", traceId, ex.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error("ACCOUNT_FROZEN", ex.getMessage(), traceId);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MaxAccountsExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxAccountsExceededException(
            MaxAccountsExceededException ex,
            WebRequest request
    ) {
        String traceId = UUID.randomUUID().toString();
        log.error("Max accounts exceeded - TraceId: {}, Message: {}", traceId, ex.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error("MAX_ACCOUNTS_EXCEEDED", ex.getMessage(), traceId);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(InvalidAccountOperationException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidAccountOperationException(
            InvalidAccountOperationException ex,
            WebRequest request
    ) {
        String traceId = UUID.randomUUID().toString();
        log.error("Invalid account operation - TraceId: {}, Message: {}", traceId, ex.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error("INVALID_OPERATION", ex.getMessage(), traceId);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException ex,
            WebRequest request
    ) {
        String traceId = UUID.randomUUID().toString();
        List<String> errors = new ArrayList<>();
        
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add(error.getField() + ": " + error.getDefaultMessage());
        }
        
        log.error("Validation failed - TraceId: {}, Errors: {}", traceId, errors);
        
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .error(ApiResponse.ErrorDetails.builder()
                        .code("VALIDATION_ERROR")
                        .message("Validation failed")
                        .details(errors)
                        .build())
                .traceId(traceId)
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(
            AuthenticationException ex,
            WebRequest request
    ) {
        String traceId = UUID.randomUUID().toString();
        log.error("Authentication failed - TraceId: {}, Message: {}", traceId, ex.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error("AUTHENTICATION_FAILED", "Authentication failed", traceId);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(
            AccessDeniedException ex,
            WebRequest request
    ) {
        String traceId = UUID.randomUUID().toString();
        log.error("Access denied - TraceId: {}, Message: {}", traceId, ex.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error("ACCESS_DENIED", "Access denied", traceId);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(
            Exception ex,
            WebRequest request
    ) {
        String traceId = UUID.randomUUID().toString();
        log.error("Unexpected error - TraceId: {}, Message: {}", traceId, ex.getMessage(), ex);
        
        ApiResponse<Void> response = ApiResponse.error("INTERNAL_SERVER_ERROR", "An unexpected error occurred", traceId);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
