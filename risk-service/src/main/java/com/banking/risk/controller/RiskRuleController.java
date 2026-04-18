package com.banking.risk.controller;

import com.banking.risk.dto.ApiResponse;
import com.banking.risk.dto.RiskRuleRequest;
import com.banking.risk.dto.RiskRuleResponse;
import com.banking.risk.service.RiskRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for risk rule management.
 * Provides endpoints for creating, updating, and managing risk rules.
 * All endpoints require ADMIN role.
 */
@RestController
@RequestMapping("/v1/risk/rules")
@Tag(name = "Risk Rules", description = "Risk rule management endpoints (Admin only)")
@SecurityRequirement(name = "Bearer Authentication")
public class RiskRuleController {

    private static final Logger logger = LoggerFactory.getLogger(RiskRuleController.class);

    private final RiskRuleService riskRuleService;

    public RiskRuleController(RiskRuleService riskRuleService) {
        this.riskRuleService = riskRuleService;
    }

    /**
     * Create a new risk rule.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create risk rule", 
               description = "Create a new risk assessment rule (Admin only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Risk rule created successfully",
                    content = @Content(schema = @Schema(implementation = RiskRuleResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Admin role required"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Risk rule with this name already exists"
            )
    })
    public ResponseEntity<ApiResponse<RiskRuleResponse>> createRiskRule(
            @Valid @RequestBody RiskRuleRequest request
    ) {
        logger.info("Creating risk rule: {}", request.name());

        RiskRuleResponse response = riskRuleService.createRiskRule(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, getTraceId()));
    }

    /**
     * Update an existing risk rule.
     */
    @PutMapping("/{ruleId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update risk rule", 
               description = "Update an existing risk rule (Admin only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Risk rule updated successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Risk rule not found"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Risk rule with this name already exists"
            )
    })
    public ResponseEntity<ApiResponse<RiskRuleResponse>> updateRiskRule(
            @PathVariable UUID ruleId,
            @Valid @RequestBody RiskRuleRequest request
    ) {
        logger.info("Updating risk rule: {}", ruleId);

        RiskRuleResponse response = riskRuleService.updateRiskRule(ruleId, request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response, getTraceId()));
    }

    /**
     * Get risk rule by ID.
     */
    @GetMapping("/{ruleId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get risk rule by ID", 
               description = "Retrieve a specific risk rule (Admin only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Risk rule retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Risk rule not found"
            )
    })
    public ResponseEntity<ApiResponse<RiskRuleResponse>> getRiskRuleById(
            @PathVariable UUID ruleId
    ) {
        logger.debug("Retrieving risk rule: {}", ruleId);

        RiskRuleResponse response = riskRuleService.getRiskRuleById(ruleId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response, getTraceId()));
    }

    /**
     * Get all risk rules.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all risk rules", 
               description = "Retrieve all risk rules with pagination (Admin only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Risk rules retrieved successfully"
            )
    })
    public ResponseEntity<ApiResponse<Page<RiskRuleResponse>>> getAllRiskRules(
            @PageableDefault(size = 20, sort = "priority", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        logger.debug("Retrieving all risk rules");

        Page<RiskRuleResponse> response = riskRuleService.getAllRiskRules(pageable);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response, getTraceId()));
    }

    /**
     * Delete a risk rule.
     */
    @DeleteMapping("/{ruleId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete risk rule", 
               description = "Soft delete a risk rule (Admin only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "204",
                    description = "Risk rule deleted successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Risk rule not found"
            )
    })
    public ResponseEntity<Void> deleteRiskRule(@PathVariable UUID ruleId) {
        logger.info("Deleting risk rule: {}", ruleId);

        riskRuleService.deleteRiskRule(ruleId);

        return ResponseEntity.noContent().build();
    }

    /**
     * Toggle risk rule enabled status.
     */
    @PatchMapping("/{ruleId}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Toggle risk rule", 
               description = "Enable or disable a risk rule (Admin only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Risk rule toggled successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Risk rule not found"
            )
    })
    public ResponseEntity<ApiResponse<RiskRuleResponse>> toggleRiskRule(
            @PathVariable UUID ruleId,
            @RequestParam boolean enabled
    ) {
        logger.info("Toggling risk rule: id={}, enabled={}", ruleId, enabled);

        RiskRuleResponse response = riskRuleService.toggleRiskRule(ruleId, enabled);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response, getTraceId()));
    }

    /**
     * Get trace ID from MDC.
     */
    private String getTraceId() {
        String traceId = MDC.get("traceId");
        return traceId != null ? traceId : UUID.randomUUID().toString();
    }
}
