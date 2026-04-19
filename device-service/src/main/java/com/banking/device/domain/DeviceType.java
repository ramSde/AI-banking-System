package com.banking.device.domain;

/**
 * Enumeration of supported device types for device intelligence classification.
 * Used for device fingerprinting and risk assessment.
 */
public enum DeviceType {
    /**
     * Desktop computers, laptops, workstations
     */
    DESKTOP,
    
    /**
     * Mobile phones, smartphones
     */
    MOBILE,
    
    /**
     * Tablets, iPads, Android tablets
     */
    TABLET,
    
    /**
     * Unknown or unidentifiable device type
     */
    UNKNOWN
}