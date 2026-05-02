package com.banking.vision.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for data extraction from OCR text.
 * Provides helper methods for extracting structured data like dates, amounts, emails, etc.
 *
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2026-05-02
 */
@Slf4j
@Component
public class DataExtractionUtil {

    // Regex patterns for common data types
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "\\+?\\d{1,3}?[-.\\s]?\\(?\\d{1,4}\\)?[-.\\s]?\\d{1,4}[-.\\s]?\\d{1,9}"
    );

    private static final Pattern AMOUNT_PATTERN = Pattern.compile(
            "[$€£¥]?\\s*\\d{1,3}(?:,\\d{3})*(?:\\.\\d{2})?|\\d+\\.\\d{2}"
    );

    private static final Pattern DATE_PATTERN = Pattern.compile(
            "\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4}|\\d{4}[/-]\\d{1,2}[/-]\\d{1,2}"
    );

    private static final Pattern ACCOUNT_NUMBER_PATTERN = Pattern.compile(
            "\\b\\d{8,17}\\b"
    );

    private static final Pattern ROUTING_NUMBER_PATTERN = Pattern.compile(
            "\\b\\d{9}\\b"
    );

    private static final Pattern ZIP_CODE_PATTERN = Pattern.compile(
            "\\b\\d{5}(?:-\\d{4})?\\b"
    );

    private static final Pattern SSN_PATTERN = Pattern.compile(
            "\\b\\d{3}-\\d{2}-\\d{4}\\b"
    );

    // Date formatters
    private static final List<DateTimeFormatter> DATE_FORMATTERS = Arrays.asList(
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("MM-dd-yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("M/d/yyyy"),
            DateTimeFormatter.ofPattern("d/M/yyyy"),
            DateTimeFormatter.ofPattern("MM/dd/yy"),
            DateTimeFormatter.ofPattern("dd/MM/yy")
    );

    /**
     * Extract all email addresses from text.
     *
     * @param text Input text
     * @return List of email addresses
     */
    public List<String> extractEmails(String text) {
        if (text == null || text.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> emails = new ArrayList<>();
        Matcher matcher = EMAIL_PATTERN.matcher(text);

        while (matcher.find()) {
            emails.add(matcher.group());
        }

        log.debug("Extracted {} email addresses", emails.size());
        return emails;
    }

    /**
     * Extract all phone numbers from text.
     *
     * @param text Input text
     * @return List of phone numbers
     */
    public List<String> extractPhoneNumbers(String text) {
        if (text == null || text.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> phoneNumbers = new ArrayList<>();
        Matcher matcher = PHONE_PATTERN.matcher(text);

        while (matcher.find()) {
            String phone = matcher.group().replaceAll("[^0-9+]", "");
            if (phone.length() >= 10) {
                phoneNumbers.add(matcher.group());
            }
        }

        log.debug("Extracted {} phone numbers", phoneNumbers.size());
        return phoneNumbers;
    }

    /**
     * Extract all monetary amounts from text.
     *
     * @param text Input text
     * @return List of amounts as BigDecimal
     */
    public List<BigDecimal> extractAmounts(String text) {
        if (text == null || text.isEmpty()) {
            return Collections.emptyList();
        }

        List<BigDecimal> amounts = new ArrayList<>();
        Matcher matcher = AMOUNT_PATTERN.matcher(text);

        while (matcher.find()) {
            try {
                String amountStr = matcher.group()
                        .replaceAll("[$€£¥,\\s]", "");
                BigDecimal amount = new BigDecimal(amountStr);
                amounts.add(amount);
            } catch (NumberFormatException e) {
                log.warn("Failed to parse amount: {}", matcher.group());
            }
        }

        log.debug("Extracted {} amounts", amounts.size());
        return amounts;
    }

    /**
     * Extract the largest amount from text (typically the total).
     *
     * @param text Input text
     * @return Largest amount or null if none found
     */
    public BigDecimal extractLargestAmount(String text) {
        List<BigDecimal> amounts = extractAmounts(text);
        return amounts.stream()
                .max(BigDecimal::compareTo)
                .orElse(null);
    }

    /**
     * Extract all dates from text.
     *
     * @param text Input text
     * @return List of dates
     */
    public List<LocalDate> extractDates(String text) {
        if (text == null || text.isEmpty()) {
            return Collections.emptyList();
        }

        List<LocalDate> dates = new ArrayList<>();
        Matcher matcher = DATE_PATTERN.matcher(text);

        while (matcher.find()) {
            String dateStr = matcher.group();
            LocalDate date = parseDate(dateStr);
            if (date != null) {
                dates.add(date);
            }
        }

        log.debug("Extracted {} dates", dates.size());
        return dates;
    }

    /**
     * Parse date string using multiple formats.
     *
     * @param dateStr Date string
     * @return LocalDate or null if parsing fails
     */
    public LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }

        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(dateStr, formatter);
            } catch (DateTimeParseException e) {
                // Try next formatter
            }
        }

        log.warn("Failed to parse date: {}", dateStr);
        return null;
    }

    /**
     * Extract account numbers from text.
     *
     * @param text Input text
     * @return List of account numbers
     */
    public List<String> extractAccountNumbers(String text) {
        if (text == null || text.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> accountNumbers = new ArrayList<>();
        Matcher matcher = ACCOUNT_NUMBER_PATTERN.matcher(text);

        while (matcher.find()) {
            accountNumbers.add(matcher.group());
        }

        log.debug("Extracted {} account numbers", accountNumbers.size());
        return accountNumbers;
    }

    /**
     * Extract routing numbers from text.
     *
     * @param text Input text
     * @return List of routing numbers
     */
    public List<String> extractRoutingNumbers(String text) {
        if (text == null || text.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> routingNumbers = new ArrayList<>();
        Matcher matcher = ROUTING_NUMBER_PATTERN.matcher(text);

        while (matcher.find()) {
            routingNumbers.add(matcher.group());
        }

        log.debug("Extracted {} routing numbers", routingNumbers.size());
        return routingNumbers;
    }

    /**
     * Extract ZIP codes from text.
     *
     * @param text Input text
     * @return List of ZIP codes
     */
    public List<String> extractZipCodes(String text) {
        if (text == null || text.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> zipCodes = new ArrayList<>();
        Matcher matcher = ZIP_CODE_PATTERN.matcher(text);

        while (matcher.find()) {
            zipCodes.add(matcher.group());
        }

        log.debug("Extracted {} ZIP codes", zipCodes.size());
        return zipCodes;
    }

    /**
     * Extract SSN from text.
     *
     * @param text Input text
     * @return List of SSNs
     */
    public List<String> extractSSN(String text) {
        if (text == null || text.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> ssns = new ArrayList<>();
        Matcher matcher = SSN_PATTERN.matcher(text);

        while (matcher.find()) {
            ssns.add(matcher.group());
        }

        log.debug("Extracted {} SSNs", ssns.size());
        return ssns;
    }

    /**
     * Extract value after a label (e.g., "Total: $100.00").
     *
     * @param text  Input text
     * @param label Label to search for
     * @return Value after label or null if not found
     */
    public String extractValueAfterLabel(String text, String label) {
        if (text == null || label == null) {
            return null;
        }

        Pattern pattern = Pattern.compile(
                Pattern.quote(label) + "\\s*:?\\s*([^\\n\\r]+)",
                Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        return null;
    }

    /**
     * Extract all key-value pairs from text (format: "Key: Value").
     *
     * @param text Input text
     * @return Map of key-value pairs
     */
    public Map<String, String> extractKeyValuePairs(String text) {
        if (text == null || text.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, String> pairs = new HashMap<>();
        Pattern pattern = Pattern.compile("([^:\\n]+):\\s*([^\\n]+)");
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            String key = matcher.group(1).trim();
            String value = matcher.group(2).trim();
            pairs.put(key, value);
        }

        log.debug("Extracted {} key-value pairs", pairs.size());
        return pairs;
    }

    /**
     * Clean and normalize text.
     *
     * @param text Input text
     * @return Cleaned text
     */
    public String cleanText(String text) {
        if (text == null) {
            return "";
        }

        return text
                .replaceAll("\\s+", " ")  // Replace multiple spaces with single space
                .replaceAll("[\\r\\n]+", "\n")  // Normalize line breaks
                .trim();
    }

    /**
     * Extract lines containing a keyword.
     *
     * @param text    Input text
     * @param keyword Keyword to search for
     * @return List of lines containing the keyword
     */
    public List<String> extractLinesContaining(String text, String keyword) {
        if (text == null || keyword == null) {
            return Collections.emptyList();
        }

        List<String> lines = new ArrayList<>();
        String[] textLines = text.split("[\\r\\n]+");

        for (String line : textLines) {
            if (line.toLowerCase().contains(keyword.toLowerCase())) {
                lines.add(line.trim());
            }
        }

        return lines;
    }

    /**
     * Calculate confidence score based on extracted data completeness.
     *
     * @param extractedFields Number of successfully extracted fields
     * @param totalFields     Total number of expected fields
     * @return Confidence score (0-100)
     */
    public double calculateConfidenceScore(int extractedFields, int totalFields) {
        if (totalFields == 0) {
            return 0.0;
        }

        return (double) extractedFields / totalFields * 100.0;
    }

    /**
     * Check if text contains any of the keywords.
     *
     * @param text     Input text
     * @param keywords Keywords to search for
     * @return true if any keyword is found
     */
    public boolean containsAnyKeyword(String text, String... keywords) {
        if (text == null || keywords == null) {
            return false;
        }

        String lowerText = text.toLowerCase();
        for (String keyword : keywords) {
            if (lowerText.contains(keyword.toLowerCase())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Count occurrences of a keyword in text.
     *
     * @param text    Input text
     * @param keyword Keyword to count
     * @return Number of occurrences
     */
    public int countKeywordOccurrences(String text, String keyword) {
        if (text == null || keyword == null) {
            return 0;
        }

        String lowerText = text.toLowerCase();
        String lowerKeyword = keyword.toLowerCase();
        int count = 0;
        int index = 0;

        while ((index = lowerText.indexOf(lowerKeyword, index)) != -1) {
            count++;
            index += lowerKeyword.length();
        }

        return count;
    }
}
