package com.banking.vision.domain;

/**
 * Enumeration of supported document types for vision processing.
 * 
 * Each type has specific extraction templates and validation rules.
 */
public enum DocumentType {
    /**
     * Retail receipts - Extract merchant, date, total, line items
     */
    RECEIPT,
    
    /**
     * Business invoices - Extract vendor, invoice number, line items, totals
     */
    INVOICE,
    
    /**
     * Bank statements - Extract transactions, balances, account info
     */
    BANK_STATEMENT,
    
    /**
     * Checks - Extract routing number, account number, amount, payee
     */
    CHECK,
    
    /**
     * Identity documents - Extract name, DOB, ID number (KYC)
     */
    ID_DOCUMENT,
    
    /**
     * Generic documents - Full text extraction without structured parsing
     */
    GENERIC
}
