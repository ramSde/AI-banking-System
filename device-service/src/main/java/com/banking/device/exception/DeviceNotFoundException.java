package com.banking.device.exception;

/**
 * Exception thrown when a requested device is not found.
 */
public class DeviceNotFoundException extends DeviceException {

    public DeviceNotFoundException(String message) {
        super(message);
    }

    public DeviceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}