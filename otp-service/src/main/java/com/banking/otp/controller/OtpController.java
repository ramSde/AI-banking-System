package com.banking.otp.controller;

import com.banking.otp.dto.ApiResponse;
import com.banking.otp.dto.SendOtpRequest;
import com.banking.otp.dto.VerifyOtpRequest;
import com.banking.otp.dto.EnrollTotpRequest;
import com.banking.otp.dto.EnrollTotpResponse;
import com.banking.otp.dto.VerifyTotpRequest;
import com.banking.otp.service.OtpService;
import com.banking.otp.service.TotpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for OTP operations
 */
@RestController
@RequestMapping("/v1/otp")
@Tag(name = "OTP Operations", description = "Endpoints for OTP generation and verification")
public class OtpController {

    private static final Logger logger = LoggerFactory.getLogger(OtpController.class);

    private final OtpService otpService;
    private final TotpService totpService;

    public OtpController(OtpService otpService, TotpService totpService) {
        this.otpService = otpService;
        this.totpService = totpService;
    }

    @PostMapping("/totp/enroll")
    @Operation(summary = "Enroll in TOTP", description = "Enroll user in TOTP-based MFA (Google Authenticator)")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "TOTP enrollment initiated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Already enrolled", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<ApiResponse<EnrollTotpResponse>> enrollTotp(@Valid @RequestBody EnrollTotpRequest request) {
        logger.info("TOTP enrollment request for user {}", request.userId());
        EnrollTotpResponse response = totpService.enrollTotp(request.userId(), request.userId().toString());
        return ResponseEntity.ok(ApiResponse.success(response, UUID.randomUUID().toString()));
    }

    @PostMapping("/totp/verify-enrollment")
    @Operation(summary = "Verify TOTP enrollment", description = "Verify TOTP code to complete enrollment")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "TOTP enrollment verified successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid TOTP code", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Enrollment not found", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<ApiResponse<Boolean>> verifyTotpEnrollment(@Valid @RequestBody VerifyTotpRequest request) {
        logger.info("TOTP enrollment verification request for user {}", request.userId());
        boolean verified = totpService.verifyTotpEnrollment(request.userId(), request.code());
        return ResponseEntity.ok(ApiResponse.success(verified, UUID.randomUUID().toString()));
    }

    @PostMapping("/totp/verify")
    @Operation(summary = "Verify TOTP", description = "Verify TOTP code for authentication")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "TOTP verified successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid TOTP code", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not enrolled", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<ApiResponse<Boolean>> verifyTotp(@Valid @RequestBody VerifyTotpRequest request) {
        logger.info("TOTP verification request for user {}", request.userId());
        boolean verified = totpService.verifyTotp(request.userId(), request.code());
        return ResponseEntity.ok(ApiResponse.success(verified, UUID.randomUUID().toString()));
    }

    @PostMapping("/send")
    @Operation(summary = "Send OTP", description = "Send OTP via SMS or Email")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OTP sent successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "429", description = "Rate limit exceeded", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<ApiResponse<Void>> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        logger.info("OTP send request for user {} via {}", request.userId(), request.method());
        String destination = request.method().name().equals("SMS") ? request.phoneNumber() : request.email();
        otpService.sendOtp(request.userId(), request.method(), destination);
        return ResponseEntity.ok(ApiResponse.success(null, UUID.randomUUID().toString()));
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify OTP", description = "Verify OTP code sent via SMS or Email")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OTP verified successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid or expired OTP", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<ApiResponse<Boolean>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        logger.info("OTP verification request for user {} via {}", request.userId(), request.method());
        boolean verified = otpService.verifyOtp(request.userId(), request.method(), request.code());
        return ResponseEntity.ok(ApiResponse.success(verified, UUID.randomUUID().toString()));
    }
}
