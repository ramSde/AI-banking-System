package com.banking.user.domain;

/**
 * Enumeration of KYC (Know Your Customer) verification status.
 * Tracks compliance with regulatory requirements.
 */
public enum KycStatus {
    /**
     * KYC verification not started
     */
    NOT_STARTED,
    
    /**
     * KYC documents submitted, pending review
     */
    PENDING,
    
    /**
     * KYC verification in progress
     */
    IN_PROGRESS,
    
    /**
     * KYC verification completed successfully
     */
    VERIFIED,
    
    /**
     * KYC verification rejected
     */
    REJECTED,
    
    /**
     * KYC verification expired, needs renewal
     */
    EXPIRED
}
