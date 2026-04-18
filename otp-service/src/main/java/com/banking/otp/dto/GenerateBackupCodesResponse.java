package com.banking.otp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Response containing generated backup codes
 */
@Schema(description = "Response containing generated backup codes")
public record GenerateBackupCodesResponse(
        @Schema(description = "List of backup codes (plain text - show only once)", example = "[\"ABCD-1234-EFGH\", \"IJKL-5678-MNOP\"]")
        List<String> codes,

        @Schema(description = "Number of codes generated", example = "8")
        int count,

        @Schema(description = "Warning message", example = "Save these codes in a secure location. They will not be shown again.")
        String warning
) {}
