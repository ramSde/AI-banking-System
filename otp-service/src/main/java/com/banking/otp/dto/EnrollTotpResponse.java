package com.banking.otp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response containing TOTP enrollment details
 */
@Schema(description = "Response containing TOTP enrollment details")
public record EnrollTotpResponse(
        @Schema(description = "Base32-encoded TOTP secret", example = "JBSWY3DPEHPK3PXP")
        String secret,

        @Schema(description = "QR code as Base64-encoded PNG image")
        String qrCode,

        @Schema(description = "Manual entry key (formatted secret)", example = "JBSW-Y3DP-EHPK-3PXP")
        String manualEntryKey,

        @Schema(description = "Issuer name", example = "BankingPlatform")
        String issuer,

        @Schema(description = "Account name (typically user email or ID)", example = "user@example.com")
        String accountName
) {}
