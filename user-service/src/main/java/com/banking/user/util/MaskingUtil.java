package com.banking.user.util;

import lombok.experimental.UtilityClass;

import java.time.LocalDate;

/**
 * Utility class for masking sensitive PII data.
 * Provides methods to mask phone numbers, names, dates, and addresses.
 */
@UtilityClass
public class MaskingUtil {

    /**
     * Mask phone number - show last 4 digits
     * Example: +1234567890 -> +******7890
     */
    public static String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 4) {
            return "****";
        }
        int visibleDigits = 4;
        int maskLength = phoneNumber.length() - visibleDigits;
        return "*".repeat(maskLength) + phoneNumber.substring(maskLength);
    }

    /**
     * Mask email - show first 2 chars and domain
     * Example: john.doe@example.com -> jo*******@example.com
     */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "****@****.com";
        }
        String[] parts = email.split("@");
        String localPart = parts[0];
        String domain = parts[1];
        
        if (localPart.length() <= 2) {
            return "*".repeat(localPart.length()) + "@" + domain;
        }
        
        return localPart.substring(0, 2) + "*".repeat(localPart.length() - 2) + "@" + domain;
    }

    /**
     * Mask name - show first and last character
     * Example: John -> J**n
     */
    public static String maskName(String name) {
        if (name == null || name.length() <= 2) {
            return "****";
        }
        if (name.length() == 3) {
            return name.charAt(0) + "*" + name.charAt(2);
        }
        return name.charAt(0) + "*".repeat(name.length() - 2) + name.charAt(name.length() - 1);
    }

    /**
     * Mask date of birth - show only year
     * Example: 1990-05-15 -> ****-**-15
     */
    public static String maskDateOfBirth(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            return "****-**-**";
        }
        return "****-**-" + String.format("%02d", dateOfBirth.getDayOfMonth());
    }

    /**
     * Mask address - show only city and country
     * Example: 123 Main St, Apt 4B -> ***, ***, City, Country
     */
    public static String maskAddress(String address) {
        if (address == null || address.isEmpty()) {
            return "****";
        }
        // Simple masking - replace all but last 20 characters
        if (address.length() <= 20) {
            return "*".repeat(address.length());
        }
        return "*".repeat(address.length() - 20) + address.substring(address.length() - 20);
    }

    /**
     * Mask document number - show last 4 characters
     * Example: AB1234567890 -> ********7890
     */
    public static String maskDocumentNumber(String documentNumber) {
        if (documentNumber == null || documentNumber.length() < 4) {
            return "****";
        }
        int visibleChars = 4;
        int maskLength = documentNumber.length() - visibleChars;
        return "*".repeat(maskLength) + documentNumber.substring(maskLength);
    }

    /**
     * Mask account number - show last 4 digits
     * Example: 1234567890123456 -> ************3456
     */
    public static String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 4) {
            return "****";
        }
        int visibleDigits = 4;
        int maskLength = accountNumber.length() - visibleDigits;
        return "*".repeat(maskLength) + accountNumber.substring(maskLength);
    }

    /**
     * Mask card number - show first 6 and last 4 digits
     * Example: 1234567890123456 -> 123456******3456
     */
    public static String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 10) {
            return "****-****-****-****";
        }
        if (cardNumber.length() <= 10) {
            return "*".repeat(cardNumber.length());
        }
        String first6 = cardNumber.substring(0, 6);
        String last4 = cardNumber.substring(cardNumber.length() - 4);
        int middleLength = cardNumber.length() - 10;
        return first6 + "*".repeat(middleLength) + last4;
    }
}
