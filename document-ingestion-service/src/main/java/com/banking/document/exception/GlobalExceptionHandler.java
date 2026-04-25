package com.banking.document.exception;

import com.banking.document.dto.ApiResponse;
import io.micrometer.tracing.Tracer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private final Tracer tracer;

    public GlobalExceptionHandler(Tracer tracer) {
        this.tracer = tracer;
    }

    @ExceptionHandler(DocumentNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleDocumentNotFoundException(DocumentNotFoundException ex) {
        String traceId = getTraceId();
        log.error("Document not found: {} - traceId: {}", ex.getMessage(), traceId);
        
        ApiResponse<Void> response = ApiResponse.error("DOCUMENT_NOT_FOUND", ex.getMessage(), traceId);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(DocumentProcessingException.class)
    public ResponseEntity<ApiResponse<Void>> handleDocumentProcessingException(DocumentProcessingException ex) {
        String traceId = getTraceId();
        log.error("Document processing error: {} - traceId: {}", ex.getMessage(), traceId, ex);
        
        ApiResponse<Void> response = ApiResponse.error("DOCUMENT_PROCESSING_ERROR", ex.getMessage(), traceId);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(UnsupportedDocumentTypeException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnsupportedDocumentTypeException(UnsupportedDocumentTypeException ex) {
        String traceId = getTraceId();
        log.error("Unsupported document type: {} - traceId: {}", ex.getMessage(), traceId);
        
        ApiResponse<Void> response = ApiResponse.error("UNSUPPORTED_DOCUMENT_TYPE", ex.getMessage(), traceId);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        String traceId = getTraceId();
        log.error("File size exceeded: {} - traceId: {}", ex.getMessage(), traceId);
        
        ApiResponse<Void> response = ApiResponse.error(
                "FILE_SIZE_EXCEEDED",
                "File size exceeds the maximum allowed limit",
                traceId
        );
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(response);
    }

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

        log.error("Validation error: {} - traceId: {}", errors, traceId);

        ApiResponse.ErrorDetails errorDetails = ApiResponse.ErrorDetails.builder()
                .code("VALIDATION_ERROR")
                .message("Validation failed")
                .details(errors)
                .build();

        ApiResponse<Void> response = ApiResponse.error(errorDetails, traceId);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException ex) {
        String traceId = getTraceId();
        log.error("Authentication error: {} - traceId: {}", ex.getMessage(), traceId);
        
        ApiResponse<Void> response = ApiResponse.error("AUTHENTICATION_ERROR", "Authentication failed", traceId);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ex) {
        String traceId = getTraceId();
        log.error("Access denied: {} - traceId: {}", ex.getMessage(), traceId);
        
        ApiResponse<Void> response = ApiResponse.error("ACCESS_DENIED", "Access denied", traceId);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        String traceId = getTraceId();
        log.error("Unexpected error: {} - traceId: {}", ex.getMessage(), traceId, ex);
        
        ApiResponse<Void> response = ApiResponse.error(
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred",
                traceId
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    private String getTraceId() {
        if (tracer != null && tracer.currentSpan() != null) {
            return tracer.currentSpan().context().traceId();
        }
        return "no-trace-id";
    }
}
