package com.banking.user.util;

import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * Utility class for validation operations.
 * Provides methods to validate email, phone, dates, and other user inputs.
 */
@UtilityClass
public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^\\+?[1-9]\\d{1,14}$" // E.164 format
    );

    private static final Pattern POSTAL_CODE_PATTERN = Pattern.compile(
            "^[A-Z0-9]{3,10}$"
    );

    private static final Pattern CURRENCY_CODE_PATTERN = Pattern.compile(
            "^[A-Z]{3}$" // ISO 4217
    );

    private static final Pattern COUNTRY_CODE_PATTERN = Pattern.compile(
            "^[A-Z]{2,3}$" // ISO 3166-1
    );

    /**
     * Validate email format
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validate phone number (E.164 format)
     */
    public static boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber != null && PHONE_PATTERN.matcher(phoneNumber).matches();
    }

    /**
     * Validate postal code
     */
    public static boolean isValidPostalCode(String postalCode) {
        return postalCode != null && POSTAL_CODE_PATTERN.matcher(postalCode.toUpperCase()).matches();
    }

    /**
     * Validate currency code (ISO 4217)
     */
    public static boolean isValidCurrencyCode(String currencyCode) {
        return currencyCode != null && CURRENCY_CODE_PATTERN.matcher(currencyCode).matches();
    }

    /**
     * Validate country code (ISO 3166-1)
     */
    public static boolean isValidCountryCode(String countryCode) {
        return countryCode != null && COUNTRY_CODE_PATTERN.matcher(countryCode).matches();
    }

    /**
     * Validate date of birth (must be at least 18 years old)
     */
    public static boolean isValidDateOfBirth(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            return false;
        }
        LocalDate eighteenYearsAgo = LocalDate.now().minusYears(18);
        return dateOfBirth.isBefore(eighteenYearsAgo) || dateOfBirth.isEqual(eighteenYearsAgo);
    }

    /**
     * Validate age range
     */
    public static boolean isAgeInRange(LocalDate dateOfBirth, int minAge, int maxAge) {
        if (dateOfBirth == null) {
            return false;
        }
        LocalDate minDate = LocalDate.now().minusYears(maxAge);
        LocalDate maxDate = LocalDate.now().minusYears(minAge);
        return !dateOfBirth.isBefore(minDate) && !dateOfBirth.isAfter(maxDate);
    }

    /**
     * Validate name (letters, spaces, hyphens only)
     */
    public static boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        return name.matches("^[a-zA-Z\\s'-]{2,100}$");
    }

    /**
     * Validate timezone
     */
    public static boolean isValidTimezone(String timezone) {
        if (timezone == null || timezone.trim().isEmpty()) {
            return false;
        }
        try {
            java.time.ZoneId.of(timezone);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validate language code (ISO 639-1)
     */
    public static boolean isValidLanguageCode(String languageCode) {
        if (languageCode == null) {
            return false;
        }
        return languageCode.matches("^[a-z]{2}$");
    }

    /**
     * Validate session timeout (5-120 minutes)
     */
    public static boolean isValidSessionTimeout(Integer minutes) {
        return minutes != null && minutes >= 5 && minutes <= 120;
    }

    /**
     * Validate file size (max 10MB)
     */
    public static boolean isValidFileSize(Long sizeBytes, Long maxSizeBytes) {
        return sizeBytes != null && sizeBytes > 0 && sizeBytes <= maxSizeBytes;
    }

    /**
     * Validate MIME type for documents
     */
    public static boolean isValidDocumentMimeType(String mimeType) {
        if (mimeType == null) {
            return false;
        }
        return mimeType.equals("application/pdf") ||
               mimeType.equals("image/jpeg") ||
               mimeType.equals("image/jpg") ||
               mimeType.equals("image/png");
    }
}
