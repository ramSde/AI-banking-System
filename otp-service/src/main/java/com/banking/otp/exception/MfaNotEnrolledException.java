package com.banking.otp.exception;

/**
 * Exception thrown when user tries to use MFA method they're not enrolled in
 */
public class MfaNotEnrolledException extends OtpException {
    
    public MfaNotEnrolledException(String message) {
        super(message);
    }
}
