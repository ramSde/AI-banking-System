package com.banking.device.domain;

/**
 * Enumeration of device status values for security and trust management.
 * Determines how the device is treated in authentication flows.
 */
public enum DeviceStatus {
    /**
     * Device is active and can be used normally
     */
    ACTIVE,
    
    /**
     * Device is blocked due to security concerns
     */
    BLOCKED,
    
    /**
     * Device is under review for suspicious activity
     */
    SUSPICIOUS,
    
    /**
     * Device is explicitly trusted and verified
     */
    TRUSTED
}