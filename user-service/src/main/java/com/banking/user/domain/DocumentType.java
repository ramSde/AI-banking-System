package com.banking.user.domain;

/**
 * Enumeration of KYC document types for identity verification.
 * Defines acceptable documents for regulatory compliance.
 */
public enum DocumentType {
    /**
     * Government-issued passport
     */
    PASSPORT,
    
    /**
     * National identity card
     */
    NATIONAL_ID,
    
    /**
     * Driver's license
     */
    DRIVERS_LICENSE,
    
    /**
     * Proof of address (utility bill, bank statement)
     */
    PROOF_OF_ADDRESS,
    
    /**
     * Social Security Number document
     */
    SSN_CARD,
    
    /**
     * Tax identification number document
     */
    TAX_ID,
    
    /**
     * Birth certificate
     */
    BIRTH_CERTIFICATE,
    
    /**
     * Other supporting documents
     */
    OTHER
}
