package com.banking.otp.exception;

/**
 * Exception thrown when OTP has expired
 */
public class OtpExpiredException extends OtpException {
    
    public OtpExpiredException(String message) {
        super(message);
    }
}
