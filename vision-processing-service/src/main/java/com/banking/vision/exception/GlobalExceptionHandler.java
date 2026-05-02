package com.banking.vision.exception;

import com.banking.vision.dto.ApiResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
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
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Global exception handler for all vision processing service endpoints.
 * 
 * Catches exceptions and converts them to standardized ApiResponse format.
 * Never exposes stack traces or internal details to clients.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle document not found exceptions.
     */
    @ExceptionHandler(DocumentNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleDocumentNotFound(DocumentNotFoundException ex) {
        String traceId = getTraceId();
        logger.warn("Document not found: {} [traceId={}]", ex.getMessage(), traceId);
        
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error(ex.getErrorCode(), ex.getMessage(), traceId));
    }

    /**
     * Handle unsupported document type exceptions.
     */
    @ExceptionHandler(UnsupportedDocumentTypeException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnsupportedDocumentType(UnsupportedDocumentTypeException ex) {
        String traceId = getTraceId();
        logger.warn("Unsupported document type: {} [traceId={}]", ex.getMessage(), traceId);
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(ex.getErrorCode(), ex.getMessage(), traceId));
    }

    /**
     * Handle OCR processing exceptions.
     */
    @ExceptionHandler(OcrProcessingException.class)
    public ResponseEntity<ApiResponse<Void>> handleOcrProcessing(OcrProcessingException ex) {
        String traceId = getTraceId();
        logger.error("OCR processing failed: {} [traceId={}]", ex.getMessage(), traceId, ex);
        
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error(ex.getErrorCode(), "Document processing failed. Please try again or contact support.", traceId));
    }

    /**
     * Handle invalid document exceptions.
     */
    @ExceptionHandler(InvalidDocumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidDocument(InvalidDocumentException ex) {
        String traceId = getTraceId();
        logger.warn("Invalid document: {} [traceId={}]", ex.getMessage(), traceId);
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(ex.getErrorCode(), ex.getMessage(), traceId));
    }

    /**
     * Handle file size exceeded exceptions.
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
        String traceId = getTraceId();
        logger.warn("File size exceeded: {} [traceId={}]", ex.getMessage(), traceId);
        
        return ResponseEntity
            .status(HttpStatus.PAYLOAD_TOO_LARGE)
            .body(ApiResponse.error("FILE_TOO_LARGE", "File size exceeds maximum allowed limit (10MB)", traceId));
    }

    /**
     * Handle validation exceptions (Bean Validation).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationErrors(MethodArgumentNotValidException ex) {
        String traceId = getTraceId();
        
        List<String> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.toList());
        
        logger.warn("Validation failed: {} [traceId={}]", errors, traceId);
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error("VALIDATION_ERROR", "Request validation failed", errors, traceId));
    }

    /**
     * Handle constraint violation exceptions.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException ex) {
        String traceId = getTraceId();
        
        List<String> errors = ex.getConstraintViolations()
            .stream()
            .map(ConstraintViolation::getMessage)
            .collect(Collectors.toList());
        
        logger.warn("Constraint violation: {} [traceId={}]", errors, traceId);
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error("VALIDATION_ERROR", "Request validation failed", errors, traceId));
    }

    /**
     * Handle authentication exceptions.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException ex) {
        String traceId = getTraceId();
        logger.warn("Authentication failed: {} [traceId={}]", ex.getMessage(), traceId);
        
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.error("UNAUTHORIZED", "Authentication required", traceId));
    }

    /**
     * Handle access denied exceptions.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        String traceId = getTraceId();
        logger.warn("Access denied: {} [traceId={}]", ex.getMessage(), traceId);
        
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(ApiResponse.error("FORBIDDEN", "Access denied", traceId));
    }

    /**
     * Handle all other exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        String traceId = getTraceId();
        logger.error("Unexpected error: {} [traceId={}]", ex.getMessage(), traceId, ex);
        
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("INTERNAL_ERROR", "An unexpected error occurred. Please try again later.", traceId));
    }

    /**
     * Get or generate trace ID for request tracking.
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
            logger.debug("Could not retrieve traceId from request attributes", e);
        }
        return UUID.randomUUID().toString();
    }
}
