package com.banking.otp.domain;

/**
 * Enumeration of supported MFA methods
 */
public enum MfaMethod {
    /**
     * Time-based One-Time Password (Google Authenticator, Authy, etc.)
     */
    TOTP,
    
    /**
     * SMS-based OTP
     */
    SMS,
    
    /**
     * Email-based OTP
     */
    EMAIL
}
