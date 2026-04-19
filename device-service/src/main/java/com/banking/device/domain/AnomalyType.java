package com.banking.device.domain;

/**
 * Enumeration of device anomaly types for security monitoring.
 * Used to classify different types of suspicious device behavior.
 */
public enum AnomalyType {
    /**
     * New device detected for user
     */
    NEW_DEVICE,
    
    /**
     * Significant location change detected
     */
    LOCATION_CHANGE,
    
    /**
     * Impossible travel detected (too fast between locations)
     */
    IMPOSSIBLE_TRAVEL,
    
    /**
     * Access during unusual hours
     */
    UNUSUAL_HOURS,
    
    /**
     * Multiple simultaneous locations
     */
    MULTIPLE_LOCATIONS,
    
    /**
     * Device characteristics changed
     */
    DEVICE_CHANGE,
    
    /**
     * Suspicious behavioral pattern
     */
    SUSPICIOUS_PATTERN,
    
    /**
     * Velocity anomaly (too many requests)
     */
    VELOCITY_ANOMALY,
    
    /**
     * Geofence violation
     */
    GEOFENCE_VIOLATION
}