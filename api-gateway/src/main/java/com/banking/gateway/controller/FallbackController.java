package com.banking.gateway.controller;

import com.banking.gateway.dto.ApiErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

/**
 * Fallback controller for circuit breaker scenarios.
 * 
 * Provides fallback responses when downstream services are unavailable:
 * - Service-specific fallback messages
 * - Consistent error response format
 * - Appropriate HTTP status codes
 * - Logging for monitoring and alerting
 * 
 * This controller is invoked by Spring Cloud Gateway's circuit breaker
 * when downstream services fail or are unavailable.
 * 
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/identity")
    @PostMapping("/identity")
    public ResponseEntity<ApiErrorResponse> identityServiceFallback() {
        log.warn("Identity service fallback triggered - service unavailable");
        return createFallbackResponse("IDENTITY_SERVICE_UNAVAILABLE", 
                                    "Identity service is temporarily unavailable. Please try again later.");
    }

    @GetMapping("/user")
    @PostMapping("/user")
    public ResponseEntity<ApiErrorResponse> userServiceFallback() {
        log.warn("User service fallback triggered - service unavailable");
        return createFallbackResponse("USER_SERVICE_UNAVAILABLE", 
                                    "User service is temporarily unavailable. Please try again later.");
    }

    @GetMapping("/account")
    @PostMapping("/account")
    public ResponseEntity<ApiErrorResponse> accountServiceFallback() {
        log.warn("Account service fallback triggered - service unavailable");
        return createFallbackResponse("ACCOUNT_SERVICE_UNAVAILABLE", 
                                    "Account service is temporarily unavailable. Please try again later.");
    }

    @GetMapping("/transaction")
    @PostMapping("/transaction")
    public ResponseEntity<ApiErrorResponse> transactionServiceFallback() {
        log.warn("Transaction service fallback triggered - service unavailable");
        return createFallbackResponse("TRANSACTION_SERVICE_UNAVAILABLE", 
                                    "Transaction service is temporarily unavailable. Please try again later.");
    }

    @GetMapping("/fraud")
    @PostMapping("/fraud")
    public ResponseEntity<ApiErrorResponse> fraudServiceFallback() {
        log.warn("Fraud service fallback triggered - service unavailable");
        return createFallbackResponse("FRAUD_SERVICE_UNAVAILABLE", 
                                    "Fraud detection service is temporarily unavailable. Please try again later.");
    }

    @GetMapping("/audit")
    @PostMapping("/audit")
    public ResponseEntity<ApiErrorResponse> auditServiceFallback() {
        log.warn("Audit service fallback triggered - service unavailable");
        return createFallbackResponse("AUDIT_SERVICE_UNAVAILABLE", 
                                    "Audit service is temporarily unavailable. Please try again later.");
    }

    @GetMapping("/notification")
    @PostMapping("/notification")
    public ResponseEntity<ApiErrorResponse> notificationServiceFallback() {
        log.warn("Notification service fallback triggered - service unavailable");
        return createFallbackResponse("NOTIFICATION_SERVICE_UNAVAILABLE", 
                                    "Notification service is temporarily unavailable. Please try again later.");
    }

    @GetMapping("/chat")
    @PostMapping("/chat")
    public ResponseEntity<ApiErrorResponse> chatServiceFallback() {
        log.warn("Chat service fallback triggered - service unavailable");
        return createFallbackResponse("CHAT_SERVICE_UNAVAILABLE", 
                                    "Chat service is temporarily unavailable. Please try again later.");
    }

    @GetMapping("/ai")
    @PostMapping("/ai")
    public ResponseEntity<ApiErrorResponse> aiOrchestrationServiceFallback() {
        log.warn("AI orchestration service fallback triggered - service unavailable");
        return createFallbackResponse("AI_SERVICE_UNAVAILABLE", 
                                    "AI service is temporarily unavailable. Please try again later.");
    }

    @GetMapping("/document")
    @PostMapping("/document")
    public ResponseEntity<ApiErrorResponse> documentIngestionServiceFallback() {
        log.warn("Document ingestion service fallback triggered - service unavailable");
        return createFallbackResponse("DOCUMENT_SERVICE_UNAVAILABLE", 
                                    "Document service is temporarily unavailable. Please try again later.");
    }

    @GetMapping("/analytics")
    @PostMapping("/analytics")
    public ResponseEntity<ApiErrorResponse> analyticsServiceFallback() {
        log.warn("Analytics service fallback triggered - service unavailable");
        return createFallbackResponse("ANALYTICS_SERVICE_UNAVAILABLE", 
                                    "Analytics service is temporarily unavailable. Please try again later.");
    }

    @GetMapping("/statement")
    @PostMapping("/statement")
    public ResponseEntity<ApiErrorResponse> statementServiceFallback() {
        log.warn("Statement service fallback triggered - service unavailable");
        return createFallbackResponse("STATEMENT_SERVICE_UNAVAILABLE", 
                                    "Statement service is temporarily unavailable. Please try again later.");
    }

    /**
     * Creates a standardized fallback response.
     * 
     * @param errorCode Machine-readable error code
     * @param message Human-readable error message
     * @return ResponseEntity with error response
     */
    private ResponseEntity<ApiErrorResponse> createFallbackResponse(String errorCode, String message) {
        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
            .success(false)
            .error(ApiErrorResponse.ErrorDetails.builder()
                .code(errorCode)
                .message(message)
                .build())
            .traceId(UUID.randomUUID().toString())
            .timestamp(Instant.now())
            .build();

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
    }
}