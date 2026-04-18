package com.banking.identity.exception;

import com.banking.identity.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Global Exception Handler
 * 
 * Handles all exceptions thrown by controllers and translates them to standard API responses.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> handleUserNotFoundException(
            final UserNotFoundException ex,
            final HttpServletRequest request) {
        log.error("User not found: {}", ex.getMessage());
        return ApiResponse.error(ex.getErrorCode(), ex.getMessage(), getTraceId(request));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponse<Void> handleInvalidCredentialsException(
            final InvalidCredentialsException ex,
            final HttpServletRequest request) {
        log.error("Invalid credentials: {}", ex.getMessage());
        return ApiResponse.error(ex.getErrorCode(), ex.getMessage(), getTraceId(request));
    }

    @ExceptionHandler(AccountLockedException.class)
    @ResponseStatus(HttpStatus.LOCKED)
    public ApiResponse<Void> handleAccountLockedException(
            final AccountLockedException ex,
            final HttpServletRequest request) {
        log.error("Account locked: {}", ex.getMessage());
        return ApiResponse.error(ex.getErrorCode(), ex.getMessage(), getTraceId(request));
    }

    @ExceptionHandler(InvalidTokenException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponse<Void> handleInvalidTokenException(
            final InvalidTokenException ex,
            final HttpServletRequest request) {
        log.error("Invalid token: {}", ex.getMessage());
        return ApiResponse.error(ex.getErrorCode(), ex.getMessage(), getTraceId(request));
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiResponse<Void> handleUserAlreadyExistsException(
            final UserAlreadyExistsException ex,
            final HttpServletRequest request) {
        log.error("User already exists: {}", ex.getMessage());
        return ApiResponse.error(ex.getErrorCode(), ex.getMessage(), getTraceId(request));
    }

    @ExceptionHandler(WeakPasswordException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleWeakPasswordException(
            final WeakPasswordException ex,
            final HttpServletRequest request) {
        log.error("Weak password: {}", ex.getMessage());
        return ApiResponse.error(ex.getErrorCode(), ex.getMessage(), getTraceId(request));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleValidationException(
            final MethodArgumentNotValidException ex,
            final HttpServletRequest request) {
        log.error("Validation error: {}", ex.getMessage());

        final List<String> errors = new ArrayList<>();
        for (final FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add(error.getField() + ": " + error.getDefaultMessage());
        }

        return ApiResponse.error("VALIDATION_ERROR", "Request validation failed", errors, getTraceId(request));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleIllegalArgumentException(
            final IllegalArgumentException ex,
            final HttpServletRequest request) {
        log.error("Illegal argument: {}", ex.getMessage());
        return ApiResponse.error("INVALID_ARGUMENT", ex.getMessage(), getTraceId(request));
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleGenericException(
            final Exception ex,
            final HttpServletRequest request) {
        log.error("Unexpected error occurred", ex);
        return ApiResponse.error(
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred. Please try again later.",
                getTraceId(request)
        );
    }

    private String getTraceId(final HttpServletRequest request) {
        String traceId = request.getHeader("X-Trace-Id");
        if (traceId == null || traceId.isEmpty()) {
            traceId = UUID.randomUUID().toString();
        }
        return traceId;
    }
}
