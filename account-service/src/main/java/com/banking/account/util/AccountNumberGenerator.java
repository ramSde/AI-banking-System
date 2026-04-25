package com.banking.account.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * Account Number Generator
 * 
 * Generates unique account numbers following the format: {PREFIX}-{8-digit-random}
 * Example: ACC-12345678
 */
@Component
public class AccountNumberGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int ACCOUNT_NUMBER_LENGTH = 8;

    /**
     * Generate a unique account number
     * 
     * @param prefix Account number prefix (e.g., "ACC")
     * @return Generated account number
     */
    public String generate(String prefix) {
        StringBuilder accountNumber = new StringBuilder(prefix);
        accountNumber.append("-");
        
        for (int i = 0; i < ACCOUNT_NUMBER_LENGTH; i++) {
            accountNumber.append(RANDOM.nextInt(10));
        }
        
        return accountNumber.toString();
    }

    /**
     * Validate account number format
     * 
     * @param accountNumber Account number to validate
     * @param prefix Expected prefix
     * @return true if valid, false otherwise
     */
    public boolean isValid(String accountNumber, String prefix) {
        if (accountNumber == null || accountNumber.isEmpty()) {
            return false;
        }
        
        String pattern = "^" + prefix + "-\\d{" + ACCOUNT_NUMBER_LENGTH + "}$";
        return accountNumber.matches(pattern);
    }
}
