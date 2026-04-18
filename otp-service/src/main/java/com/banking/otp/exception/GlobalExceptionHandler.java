package com.banking.otp.exception;

import com.banking.otp.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * Global exception handler for OTP service
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationErrors(MethodArgumentNotValidException ex) {
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

        logger.warn("Validation error - traceId: {}, errors: {}", traceId, errors);

        ApiResponse<Void> response = ApiResponse.error("VALIDATION_ERROR", "Invalid request parameters", errors, traceId);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle invalid OTP exception
     */
    @ExceptionHandler(InvalidOtpException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidOtp(InvalidOtpException ex) {
        String traceId = getTraceId();
        logger.warn("Invalid OTP - traceId: {}, message: {}", traceId, ex.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error("INVALID_OTP", ex.getMessage(), traceId);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * Handle OTP expired exception
     */
    @ExceptionHandler(OtpExpiredException.class)
    public ResponseEntity<ApiResponse<Void>> handleOtpExpired(OtpExpiredException ex) {
        String traceId = getTraceId();
        logger.warn("OTP expired - traceId: {}, message: {}", traceId, ex.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error("OTP_EXPIRED", ex.getMessage(), traceId);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * Handle MFA already enrolled exception
     */
    @ExceptionHandler(MfaAlreadyEnrolledException.class)
    public ResponseEntity<ApiResponse<Void>> handleMfaAlreadyEnrolled(MfaAlreadyEnrolledException ex) {
        String traceId = getTraceId();
        logger.warn("MFA already enrolled - traceId: {}, message: {}", traceId, ex.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error("MFA_ALREADY_ENROLLED", ex.getMessage(), traceId);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * Handle MFA not enrolled exception
     */
    @ExceptionHandler(MfaNotEnrolledException.class)
    public ResponseEntity<ApiResponse<Void>> handleMfaNotEnrolled(MfaNotEnrolledException ex) {
        String traceId = getTraceId();
        logger.warn("MFA not enrolled - traceId: {}, message: {}", traceId, ex.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error("MFA_NOT_ENROLLED", ex.getMessage(), traceId);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Handle generic OTP exception
     */
    @ExceptionHandler(OtpException.class)
    public ResponseEntity<ApiResponse<Void>> handleOtpException(OtpException ex) {
        String traceId = getTraceId();
        logger.error("OTP error - traceId: {}, message: {}", traceId, ex.getMessage(), ex);
        
        ApiResponse<Void> response = ApiResponse.error("OTP_ERROR", ex.getMessage(), traceId);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        String traceId = getTraceId();
        logger.error("Unexpected error - traceId: {}, message: {}", traceId, ex.getMessage(), ex);
        
        ApiResponse<Void> response = ApiResponse.error(
                "INTERNAL_ERROR",
                "An unexpected error occurred. Please try again later.",
                traceId
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * Get or generate trace ID for request tracking
     */
    private String getTraceId() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                String traceId = attributes.getRequest().getHeader("X-Trace-Id");
                if (traceId != null && !traceId.isEmpty()) {
                    return traceId;
                }
            }
        } catch (Exception e) {
            logger.debug("Could not extract trace ID from request", e);
        }
        return UUID.randomUUID().toString();
    }
}
