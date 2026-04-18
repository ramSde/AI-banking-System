package com.banking.otp.service;

import com.banking.otp.dto.EnrollTotpResponse;

import java.util.UUID;

/**
 * Service interface for TOTP operations
 */
public interface TotpService {

    /**
     * Enroll user in TOTP-based MFA
     */
    EnrollTotpResponse enrollTotp(UUID userId, String accountName);

    /**
     * Verify TOTP code and complete enrollment
     */
    boolean verifyTotpEnrollment(UUID userId, String code);

    /**
     * Verify TOTP code for authentication
     */
    boolean verifyTotp(UUID userId, String code);

    /**
     * Disable TOTP for user
     */
    void disableTotp(UUID userId);
}
