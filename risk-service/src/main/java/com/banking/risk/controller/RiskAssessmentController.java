package com.banking.risk.controller;

import com.banking.risk.dto.ApiResponse;
import com.banking.risk.dto.RiskAssessmentRequest;
import com.banking.risk.dto.RiskAssessmentResponse;
import com.banking.risk.dto.RiskHistoryResponse;
import com.banking.risk.service.RiskAssessmentService;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

/**
 * REST controller for risk assessment operations.
 * Provides endpoints for assessing authentication risk and retrieving assessment history.
 */
@RestController
@RequestMapping("/v1/risk")
@Tag(name = "Risk Assessment", description = "Risk assessment and history endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class RiskAssessmentController {

    private static final Logger logger = LoggerFactory.getLogger(RiskAssessmentController.class);

    private final RiskAssessmentService riskAssessmentService;

    public RiskAssessmentController(RiskAssessmentService riskAssessmentService) {
        this.riskAssessmentService = riskAssessmentService;
    }

    /**
     * Assess authentication risk.
     */
    @PostMapping("/assess")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Assess authentication risk", 
               description = "Calculate risk score and determine required action based on authentication context")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Risk assessment completed successfully",
                    content = @Content(schema = @Schema(implementation = RiskAssessmentResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    public ResponseEntity<ApiResponse<RiskAssessmentResponse>> assessRisk(
            @Valid @RequestBody RiskAssessmentRequest request
    ) {
        logger.info("Received risk assessment request for user: {}", request.userId());

        RiskAssessmentResponse response = riskAssessmentService.assessRisk(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response, getTraceId()));
    }

    /**
     * Get risk assessment by ID.
     */
    @GetMapping("/assessment/{assessmentId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get risk assessment by ID", 
               description = "Retrieve details of a specific risk assessment")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Risk assessment retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Risk assessment not found"
            )
    })
    public ResponseEntity<ApiResponse<RiskAssessmentResponse>> getAssessmentById(
            @PathVariable UUID assessmentId
    ) {
        logger.debug("Retrieving risk assessment: {}", assessmentId);

        RiskAssessmentResponse response = riskAssessmentService.getAssessmentById(assessmentId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response, getTraceId()));
    }

    /**
     * Get risk history for a user.
     */
    @GetMapping("/history/{userId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get user risk history", 
               description = "Retrieve risk assessment history for a specific user")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Risk history retrieved successfully"
            )
    })
    public ResponseEntity<ApiResponse<Page<RiskHistoryResponse>>> getUserRiskHistory(
            @PathVariable UUID userId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        logger.debug("Retrieving risk history for user: {}", userId);

        Page<RiskHistoryResponse> response = riskAssessmentService.getUserRiskHistory(userId, pageable);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response, getTraceId()));
    }

    /**
     * Get risk history for a user within a date range.
     */
    @GetMapping("/history/{userId}/range")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get user risk history by date range", 
               description = "Retrieve risk assessment history for a user within a specific date range")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Risk history retrieved successfully"
            )
    })
    public ResponseEntity<ApiResponse<Page<RiskHistoryResponse>>> getUserRiskHistoryByDateRange(
            @PathVariable UUID userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        logger.debug("Retrieving risk history for user: {} between {} and {}", userId, startDate, endDate);

        Page<RiskHistoryResponse> response = riskAssessmentService.getUserRiskHistoryByDateRange(
                userId, startDate, endDate, pageable
        );

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
