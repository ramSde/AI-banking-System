package com.banking.otp.exception;

/**
 * Exception thrown when user tries to enroll in MFA method they're already enrolled in
 */
public class MfaAlreadyEnrolledException extends OtpException {
    
    public MfaAlreadyEnrolledException(String message) {
        super(message);
    }
}
