package com.banking.otp.exception;

/**
 * Base exception for OTP service errors
 */
public class OtpException extends RuntimeException {
    
    public OtpException(String message) {
        super(message);
    }
    
    public OtpException(String message, Throwable cause) {
        super(message, cause);
    }
}
