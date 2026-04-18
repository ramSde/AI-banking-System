package com.banking.otp.service;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for backup code operations
 */
public interface BackupCodeService {

    /**
     * Generate backup codes for user
     */
    List<String> generateBackupCodes(UUID userId);

    /**
     * Verify backup code
     */
    boolean verifyBackupCode(UUID userId, String code);

    /**
     * Get count of remaining unused backup codes
     */
    long getRemainingBackupCodesCount(UUID userId);

    /**
     * Regenerate backup codes (invalidates old ones)
     */
    List<String> regenerateBackupCodes(UUID userId);
}
