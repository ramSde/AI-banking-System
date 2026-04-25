package com.banking.fraud.controller;

import com.banking.fraud.domain.AlertStatus;
import com.banking.fraud.dto.*;
import com.banking.fraud.service.FraudAlertService;
import com.banking.fraud.service.FraudDetectionService;
import com.banking.fraud.service.FraudRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Fraud Detection Controller
 * 
 * REST API endpoints for fraud detection and management.
 */
@RestController
@RequestMapping("/v1/fraud")
@Tag(name = "Fraud Detection", description = "Fraud detection and management APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class FraudController {

    private final FraudRuleService fraudRuleService;
    private final FraudAlertService fraudAlertService;
    private final FraudDetectionService fraudDetectionService;

    public FraudController(
            FraudRuleService fraudRuleService,
            FraudAlertService fraudAlertService,
            FraudDetectionService fraudDetectionService
    ) {
        this.fraudRuleService = fraudRuleService;
        this.fraudAlertService = fraudAlertService;
        this.fraudDetectionService = fraudDetectionService;
    }

    @Operation(summary = "Create fraud rule", description = "Create a new fraud detection rule (Admin only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Rule created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping("/rules")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FraudRuleResponse>> createRule(
            @Valid @RequestBody FraudRuleRequest request,
            Authentication authentication
    ) {
        UUID createdBy = UUID.fromString(authentication.getName());
        FraudRuleResponse response = fraudRuleService.createRule(request, createdBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @Operation(summary = "Update fraud rule", description = "Update an existing fraud rule (Admin only)")
    @PutMapping("/rules/{ruleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FraudRuleResponse>> updateRule(
            @PathVariable UUID ruleId,
            @Valid @RequestBody FraudRuleRequest request
    ) {
        FraudRuleResponse response = fraudRuleService.updateRule(ruleId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Delete fraud rule", description = "Delete a fraud rule (Admin only)")
    @DeleteMapping("/rules/{ruleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteRule(@PathVariable UUID ruleId) {
        fraudRuleService.deleteRule(ruleId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "Get fraud rule", description = "Get fraud rule by ID")
    @GetMapping("/rules/{ruleId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
    public ResponseEntity<ApiResponse<FraudRuleResponse>> getRule(@PathVariable UUID ruleId) {
        FraudRuleResponse response = fraudRuleService.getRuleById(ruleId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "List fraud rules", description = "Get all fraud rules with pagination")
    @GetMapping("/rules")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
    public ResponseEntity<ApiResponse<Page<FraudRuleResponse>>> listRules(Pageable pageable) {
        Page<FraudRuleResponse> response = fraudRuleService.getAllRules(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Toggle rule status", description = "Enable or disable a fraud rule (Admin only)")
    @PatchMapping("/rules/{ruleId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FraudRuleResponse>> toggleRuleStatus(
            @PathVariable UUID ruleId,
            @RequestParam boolean enabled
    ) {
        FraudRuleResponse response = fraudRuleService.toggleRuleStatus(ruleId, enabled);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get fraud alert", description = "Get fraud alert by ID")
    @GetMapping("/alerts/{alertId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
    public ResponseEntity<ApiResponse<FraudAlertResponse>> getAlert(@PathVariable UUID alertId) {
        FraudAlertResponse response = fraudAlertService.getAlertById(alertId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "List fraud alerts", description = "Get all fraud alerts with pagination")
    @GetMapping("/alerts")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
    public ResponseEntity<ApiResponse<Page<FraudAlertResponse>>> listAlerts(Pageable pageable) {
        Page<FraudAlertResponse> response = fraudAlertService.getAllAlerts(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get alerts by status", description = "Get fraud alerts filtered by status")
    @GetMapping("/alerts/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
    public ResponseEntity<ApiResponse<Page<FraudAlertResponse>>> getAlertsByStatus(
            @PathVariable AlertStatus status,
            Pageable pageable
    ) {
        Page<FraudAlertResponse> response = fraudAlertService.getAlertsByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get open alerts", description = "Get all open fraud alerts")
    @GetMapping("/alerts/open")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
    public ResponseEntity<ApiResponse<Page<FraudAlertResponse>>> getOpenAlerts(Pageable pageable) {
        Page<FraudAlertResponse> response = fraudAlertService.getOpenAlerts(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Assign alert", description = "Assign fraud alert to a user")
    @PatchMapping("/alerts/{alertId}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
    public ResponseEntity<ApiResponse<FraudAlertResponse>> assignAlert(
            @PathVariable UUID alertId,
            @RequestParam UUID assignedTo
    ) {
        FraudAlertResponse response = fraudAlertService.assignAlert(alertId, assignedTo);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Update alert status", description = "Update fraud alert status")
    @PatchMapping("/alerts/{alertId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
    public ResponseEntity<ApiResponse<FraudAlertResponse>> updateAlertStatus(
            @PathVariable UUID alertId,
            @RequestParam AlertStatus status,
            @RequestParam(required = false) String resolutionNotes
    ) {
        FraudAlertResponse response = fraudAlertService.updateAlertStatus(alertId, status, resolutionNotes);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Perform fraud check", description = "Manually perform fraud check on a transaction (Internal use)")
    @PostMapping("/check")
    @PreAuthorize("hasRole('SYSTEM')")
    public ResponseEntity<ApiResponse<RiskScoreResponse>> performFraudCheck(
            @Valid @RequestBody FraudCheckRequest request
    ) {
        RiskScoreResponse response = fraudDetectionService.performFraudCheck(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get risk score", description = "Get cached risk score for a transaction")
    @GetMapping("/score/{transactionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT', 'SYSTEM')")
    public ResponseEntity<ApiResponse<RiskScoreResponse>> getRiskScore(@PathVariable UUID transactionId) {
        RiskScoreResponse response = fraudDetectionService.getCachedRiskScore(transactionId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Health check", description = "Service health check endpoint")
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("Fraud Detection Service is running"));
    }
}
