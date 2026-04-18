package com.banking.otp.controller;

import com.banking.otp.domain.MfaEnrollment;
import com.banking.otp.domain.MfaMethod;
import com.banking.otp.dto.ApiResponse;
import com.banking.otp.service.MfaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for MFA management operations
 */
@RestController
@RequestMapping("/v1/mfa")
@Tag(name = "MFA Management", description = "Endpoints for managing MFA enrollments")
public class MfaController {

    private static final Logger logger = LoggerFactory.getLogger(MfaController.class);

    private final MfaService mfaService;

    public MfaController(MfaService mfaService) {
        this.mfaService = mfaService;
    }

    @GetMapping("/enrollments/{userId}")
    @Operation(summary = "Get user MFA enrollments", description = "Retrieve all active MFA enrollments for a user")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "MFA enrollments retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<ApiResponse<List<MfaEnrollment>>> getUserMfaEnrollments(@PathVariable UUID userId) {
        logger.info("Getting MFA enrollments for user {}", userId);
        List<MfaEnrollment> enrollments = mfaService.getUserMfaEnrollments(userId);
        return ResponseEntity.ok(ApiResponse.success(enrollments, UUID.randomUUID().toString()));
    }

    @GetMapping("/status/{userId}")
    @Operation(summary = "Check MFA status", description = "Check if user has any active MFA enrollment")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "MFA status retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<ApiResponse<Boolean>> hasActiveMfa(@PathVariable UUID userId) {
        logger.info("Checking MFA status for user {}", userId);
        boolean hasActiveMfa = mfaService.hasActiveMfa(userId);
        return ResponseEntity.ok(ApiResponse.success(hasActiveMfa, UUID.randomUUID().toString()));
    }

    @DeleteMapping("/{userId}/{method}")
    @Operation(summary = "Disable MFA method", description = "Disable a specific MFA method for a user")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "MFA method disabled successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "MFA enrollment not found", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<ApiResponse<Void>> disableMfaMethod(
            @PathVariable UUID userId,
            @PathVariable MfaMethod method
    ) {
        logger.info("Disabling MFA method {} for user {}", method, userId);
        mfaService.disableMfaMethod(userId, method);
        return ResponseEntity.ok(ApiResponse.success(null, UUID.randomUUID().toString()));
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Disable all MFA", description = "Disable all MFA methods for a user")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "All MFA methods disabled successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<ApiResponse<Void>> disableAllMfa(@PathVariable UUID userId) {
        logger.info("Disabling all MFA methods for user {}", userId);
        mfaService.disableAllMfa(userId);
        return ResponseEntity.ok(ApiResponse.success(null, UUID.randomUUID().toString()));
    }
}
