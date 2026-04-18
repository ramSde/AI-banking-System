package com.banking.otp.service;

import com.banking.otp.domain.MfaMethod;

import java.util.UUID;

/**
 * Service interface for OTP operations (SMS/Email)
 */
public interface OtpService {

    /**
     * Send OTP via SMS or Email
     */
    void sendOtp(UUID userId, MfaMethod method, String destination);

    /**
     * Verify OTP code
     */
    boolean verifyOtp(UUID userId, MfaMethod method, String code);

    /**
     * Check if user has exceeded rate limit
     */
    boolean isRateLimited(UUID userId);
}
