package com.banking.otp.exception;

/**
 * Exception thrown when OTP validation fails
 */
public class InvalidOtpException extends OtpException {
    
    public InvalidOtpException(String message) {
        super(message);
    }
}
