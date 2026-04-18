package com.banking.otp.controller;

import com.banking.otp.dto.ApiResponse;
import com.banking.otp.dto.GenerateBackupCodesResponse;
import com.banking.otp.dto.VerifyBackupCodeRequest;
import com.banking.otp.service.BackupCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for backup code operations
 */
@RestController
@RequestMapping("/v1/backup-codes")
@Tag(name = "Backup Codes", description = "Endpoints for backup code management")
public class BackupCodeController {

    private static final Logger logger = LoggerFactory.getLogger(BackupCodeController.class);

    private final BackupCodeService backupCodeService;

    public BackupCodeController(BackupCodeService backupCodeService) {
        this.backupCodeService = backupCodeService;
    }

    @PostMapping("/generate/{userId}")
    @Operation(summary = "Generate backup codes", description = "Generate new backup codes for account recovery")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Backup codes generated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<ApiResponse<GenerateBackupCodesResponse>> generateBackupCodes(@PathVariable UUID userId) {
        logger.info("Generating backup codes for user {}", userId);
        List<String> codes = backupCodeService.generateBackupCodes(userId);
        GenerateBackupCodesResponse response = new GenerateBackupCodesResponse(
                codes,
                codes.size(),
                "Save these codes in a secure location. They will not be shown again."
        );
        return ResponseEntity.ok(ApiResponse.success(response, UUID.randomUUID().toString()));
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify backup code", description = "Verify a backup code for account recovery")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Backup code verified successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid backup code", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<ApiResponse<Boolean>> verifyBackupCode(@Valid @RequestBody VerifyBackupCodeRequest request) {
        logger.info("Verifying backup code for user {}", request.userId());
        boolean verified = backupCodeService.verifyBackupCode(request.userId(), request.code());
        return ResponseEntity.ok(ApiResponse.success(verified, UUID.randomUUID().toString()));
    }

    @GetMapping("/remaining/{userId}")
    @Operation(summary = "Get remaining backup codes count", description = "Get the count of unused backup codes")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Count retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<ApiResponse<Long>> getRemainingBackupCodesCount(@PathVariable UUID userId) {
        logger.info("Getting remaining backup codes count for user {}", userId);
        long count = backupCodeService.getRemainingBackupCodesCount(userId);
        return ResponseEntity.ok(ApiResponse.success(count, UUID.randomUUID().toString()));
    }

    @PostMapping("/regenerate/{userId}")
    @Operation(summary = "Regenerate backup codes", description = "Regenerate backup codes (invalidates old ones)")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Backup codes regenerated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<ApiResponse<GenerateBackupCodesResponse>> regenerateBackupCodes(@PathVariable UUID userId) {
        logger.info("Regenerating backup codes for user {}", userId);
        List<String> codes = backupCodeService.regenerateBackupCodes(userId);
        GenerateBackupCodesResponse response = new GenerateBackupCodesResponse(
                codes,
                codes.size(),
                "Save these codes in a secure location. They will not be shown again. Old codes have been invalidated."
        );
        return ResponseEntity.ok(ApiResponse.success(response, UUID.randomUUID().toString()));
    }
}
