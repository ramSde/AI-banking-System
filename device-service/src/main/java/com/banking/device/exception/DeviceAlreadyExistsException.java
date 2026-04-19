package com.banking.device.exception;

/**
 * Exception thrown when attempting to register a device that already exists.
 */
public class DeviceAlreadyExistsException extends DeviceException {

    public DeviceAlreadyExistsException(String message) {
        super(message);
    }

    public DeviceAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}