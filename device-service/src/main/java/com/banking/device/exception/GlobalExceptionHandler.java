package com.banking.device.exception;

import com.banking.device.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Global exception handler for device service.
 * Catches and transforms exceptions into standardized API responses.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Get trace ID from request attributes or generate new one
     */
    private String getTraceId() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                Object traceId = attributes.getRequest().getAttribute("traceId");
                if (traceId != null) {
                    return traceId.toString();
                }
            }
        } catch (Exception e) {
            log.debug("Could not retrieve trace ID from request", e);
        }
        return UUID.randomUUID().toString();
    }

    /**
     * Handle device not found exceptions
     */
    @ExceptionHandler(DeviceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleDeviceNotFoundException(DeviceNotFoundException ex) {
        String traceId = getTraceId();
        log.error("Device not found - traceId: {}, message: {}", traceId, ex.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error(
                "DEVICE_NOT_FOUND",
                ex.getMessage(),
                traceId
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Handle device already exists exceptions
     */
    @ExceptionHandler(DeviceAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleDeviceAlreadyExistsException(DeviceAlreadyExistsException ex) {
        String traceId = getTraceId();
        log.error("Device already exists - traceId: {}, message: {}", traceId, ex.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error(
                "DEVICE_ALREADY_EXISTS",
                ex.getMessage(),
                traceId
        );
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * Handle validation exceptions
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        String traceId = getTraceId();
        
        List<String> errors = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> {
                    if (error instanceof FieldError fieldError) {
                        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
                    }
                    return error.getDefaultMessage();
                })
                .collect(Collectors.toList());
        
        log.error("Validation failed - traceId: {}, errors: {}", traceId, errors);
        
        ApiResponse<Void> response = ApiResponse.error(
                "VALIDATION_ERROR",
                "Request validation failed",
                errors,
                traceId
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle generic device exceptions
     */
    @ExceptionHandler(DeviceException.class)
    public ResponseEntity<ApiResponse<Void>> handleDeviceException(DeviceException ex) {
        String traceId = getTraceId();
        log.error("Device error - traceId: {}, message: {}", traceId, ex.getMessage(), ex);
        
        ApiResponse<Void> response = ApiResponse.error(
                "DEVICE_ERROR",
                ex.getMessage(),
                traceId
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        String traceId = getTraceId();
        log.error("Unexpected error - traceId: {}, message: {}", traceId, ex.getMessage(), ex);
        
        ApiResponse<Void> response = ApiResponse.error(
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred",
                traceId
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}