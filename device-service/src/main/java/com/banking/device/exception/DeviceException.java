package com.banking.device.exception;

/**
 * Base exception for all device-related errors.
 * Extends RuntimeException for unchecked exception handling.
 */
public class DeviceException extends RuntimeException {

    public DeviceException(String message) {
        super(message);
    }

    public DeviceException(String message, Throwable cause) {
        super(message, cause);
    }
}