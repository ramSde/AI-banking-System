package com.banking.otp.service;

import com.banking.otp.domain.MfaEnrollment;
import com.banking.otp.domain.MfaMethod;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for MFA management operations
 */
public interface MfaService {

    /**
     * Get all active MFA enrollments for user
     */
    List<MfaEnrollment> getUserMfaEnrollments(UUID userId);

    /**
     * Check if user has any active MFA
     */
    boolean hasActiveMfa(UUID userId);

    /**
     * Get specific MFA enrollment
     */
    MfaEnrollment getMfaEnrollment(UUID userId, MfaMethod method);

    /**
     * Disable specific MFA method
     */
    void disableMfaMethod(UUID userId, MfaMethod method);

    /**
     * Disable all MFA methods for user
     */
    void disableAllMfa(UUID userId);
}
