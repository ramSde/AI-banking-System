package com.banking.vision.domain;

/**
 * Confidence level classification for OCR results.
 * 
 * Based on Tesseract confidence scores (0-100):
 * - HIGH: 90-100
 * - MEDIUM: 70-89
 * - LOW: 0-69
 */
public enum ConfidenceLevel {
    /**
     * High confidence (90-100%) - Results highly reliable
     */
    HIGH,
    
    /**
     * Medium confidence (70-89%) - Results generally reliable, may need review
     */
    MEDIUM,
    
    /**
     * Low confidence (0-69%) - Results unreliable, manual review required
     */
    LOW;
    
    /**
     * Determine confidence level from numeric score.
     * 
     * @param score Confidence score (0-100)
     * @return Corresponding confidence level
     */
    public static ConfidenceLevel fromScore(double score) {
        if (score >= 90.0) {
            return HIGH;
        } else if (score >= 70.0) {
            return MEDIUM;
        } else {
            return LOW;
        }
    }
}
