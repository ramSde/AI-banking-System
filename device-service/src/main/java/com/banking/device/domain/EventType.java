package com.banking.device.domain;

/**
 * Enumeration of device history event types for audit trail.
 * Tracks all significant device-related events.
 */
public enum EventType {
    /**
     * Device was first registered
     */
    REGISTERED,
    
    /**
     * Successful login from this device
     */
    LOGIN_SUCCESS,
    
    /**
     * Failed login attempt from this device
     */
    LOGIN_FAILED,
    
    /**
     * Device trust score was increased
     */
    TRUST_INCREASED,
    
    /**
     * Device trust score was decreased
     */
    TRUST_DECREASED,
    
    /**
     * Device was blocked
     */
    BLOCKED,
    
    /**
     * Device was unblocked
     */
    UNBLOCKED,
    
    /**
     * Anomaly was detected on this device
     */
    ANOMALY_DETECTED,
    
    /**
     * Device location changed significantly
     */
    LOCATION_CHANGED
}