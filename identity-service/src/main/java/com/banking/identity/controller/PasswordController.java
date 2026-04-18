package com.banking.identity.controller;

import com.banking.identity.dto.ApiResponse;
import com.banking.identity.dto.ChangePasswordRequest;
import com.banking.identity.service.PasswordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Password Controller
 * 
 * Handles password change operations.
 */
@RestController
@RequestMapping("/v1/password")
@Tag(name = "Password Management", description = "Password change and reset endpoints")
@Slf4j
public class PasswordController {

    private final PasswordService passwordService;

    public PasswordController(final PasswordService passwordService) {
        this.passwordService = passwordService;
    }

    @PostMapping("/change")
    @Operation(summary = "Change password", description = "Change user password (requires current password)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", 
                    description = "Password changed successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", 
                    description = "Invalid request data or weak password"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", 
                    description = "Invalid current password"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", 
                    description = "User not found")
    })
    public ApiResponse<Void> changePassword(
            @RequestParam final String userId,
            @Valid @RequestBody final ChangePasswordRequest request,
            final HttpServletRequest httpRequest) {
        
        log.info("Password change request received for user: {}", userId);
        
        final UUID userUuid = UUID.fromString(userId);
        passwordService.changePassword(userUuid, request.currentPassword(), request.newPassword());
        
        final String traceId = getTraceId(httpRequest);
        
        log.info("Password changed successfully for user: {}", userId);
        return ApiResponse.success(traceId);
    }

    private String getTraceId(final HttpServletRequest request) {
        String traceId = request.getHeader("X-Trace-Id");
        if (traceId == null || traceId.isEmpty()) {
            traceId = UUID.randomUUID().toString();
        }
        return traceId;
    }
}
