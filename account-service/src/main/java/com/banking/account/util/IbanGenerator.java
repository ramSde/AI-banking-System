package com.banking.account.util;

import org.springframework.stereotype.Component;

import java.math.BigInteger;

/**
 * IBAN Generator
 * 
 * Generates International Bank Account Numbers (IBAN) following ISO 13616 standard.
 * Format: {CountryCode}{CheckDigits}{BankCode}{AccountNumber}
 * Example: US12BANK12345678
 * 
 * Note: This is a simplified implementation for demonstration.
 * Production systems should use full ISO 13616 validation with country-specific rules.
 */
@Component
public class IbanGenerator {

    private static final int MOD_97 = 97;
    private static final int CHECK_DIGIT_OFFSET = 98;

    /**
     * Generate IBAN from account number
     * 
     * @param countryCode ISO 3166-1 alpha-2 country code (e.g., "US")
     * @param bankCode Bank identifier code
     * @param accountNumber Account number (digits only)
     * @return Generated IBAN
     */
    public String generate(String countryCode, String bankCode, String accountNumber) {
        String numericAccountNumber = extractDigits(accountNumber);
        
        String bban = bankCode + numericAccountNumber;
        
        String checkDigits = calculateCheckDigits(countryCode, bban);
        
        return countryCode + checkDigits + bban;
    }

    /**
     * Validate IBAN format and check digits
     * 
     * @param iban IBAN to validate
     * @return true if valid, false otherwise
     */
    public boolean isValid(String iban) {
        if (iban == null || iban.length() < 15 || iban.length() > 34) {
            return false;
        }
        
        String rearranged = iban.substring(4) + iban.substring(0, 4);
        
        String numericIban = convertToNumeric(rearranged);
        
        BigInteger ibanNumber = new BigInteger(numericIban);
        return ibanNumber.mod(BigInteger.valueOf(MOD_97)).intValue() == 1;
    }

    /**
     * Calculate check digits for IBAN
     */
    private String calculateCheckDigits(String countryCode, String bban) {
        String rearranged = bban + countryCode + "00";
        
        String numericString = convertToNumeric(rearranged);
        
        BigInteger ibanNumber = new BigInteger(numericString);
        int remainder = ibanNumber.mod(BigInteger.valueOf(MOD_97)).intValue();
        int checkDigit = CHECK_DIGIT_OFFSET - remainder;
        
        return String.format("%02d", checkDigit);
    }

    /**
     * Convert IBAN string to numeric representation
     * A=10, B=11, ..., Z=35
     */
    private String convertToNumeric(String input) {
        StringBuilder numeric = new StringBuilder();
        
        for (char c : input.toCharArray()) {
            if (Character.isDigit(c)) {
                numeric.append(c);
            } else if (Character.isLetter(c)) {
                numeric.append(Character.toUpperCase(c) - 'A' + 10);
            }
        }
        
        return numeric.toString();
    }

    /**
     * Extract only digits from account number
     */
    private String extractDigits(String accountNumber) {
        return accountNumber.replaceAll("[^0-9]", "");
    }
}
