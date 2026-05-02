package com.banking.vision.service.impl;

import com.banking.vision.domain.DocumentType;
import com.banking.vision.repository.ExtractionTemplateRepository;
import com.banking.vision.service.DataExtractionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Data extraction service implementation.
 * 
 * Extracts structured data from OCR text using:
 * - Regex patterns
 * - Template rules
 * - Heuristics
 * 
 * Supports extraction for:
 * - Receipts
 * - Invoices
 * - Checks
 * - Bank statements
 * - ID documents
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataExtractionServiceImpl implements DataExtractionService {

    private final ExtractionTemplateRepository templateRepository;

    @Override
    public Map<String, Object> extractData(String ocrText, DocumentType documentType) {
        log.info("Extracting data for document type: {}", documentType);
        
        return switch (documentType) {
            case RECEIPT -> extractReceiptData(ocrText);
            case INVOICE -> extractInvoiceData(ocrText);
            case CHECK -> extractCheckData(ocrText);
            case BANK_STATEMENT -> extractBankStatementData(ocrText);
            case ID_DOCUMENT -> extractIdDocumentData(ocrText);
            default -> Map.of("rawText", ocrText);
        };
    }

    @Override
    public Map<String, Object> extractReceiptData(String ocrText) {
        log.debug("Extracting receipt data");
        
        Map<String, Object> data = new HashMap<>();
        String[] lines = ocrText.split("\\n");
        
        // Extract merchant (usually first non-empty line)
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty() && trimmed.length() > 3) {
                data.put("merchant", trimmed);
                break;
            }
        }
        
        // Extract total
        Pattern totalPattern = Pattern.compile(
            "total[:\\s]*\\$?([0-9,]+\\.?[0-9]{0,2})",
            Pattern.CASE_INSENSITIVE
        );
        Matcher totalMatcher = totalPattern.matcher(ocrText);
        if (totalMatcher.find()) {
            String totalStr = totalMatcher.group(1).replace(",", "");
            try {
                data.put("total", new BigDecimal(totalStr));
            } catch (NumberFormatException e) {
                log.warn("Failed to parse total: {}", totalStr);
            }
        }
        
        // Extract subtotal
        Pattern subtotalPattern = Pattern.compile(
            "subtotal[:\\s]*\\$?([0-9,]+\\.?[0-9]{0,2})",
            Pattern.CASE_INSENSITIVE
        );
        Matcher subtotalMatcher = subtotalPattern.matcher(ocrText);
        if (subtotalMatcher.find()) {
            String subtotalStr = subtotalMatcher.group(1).replace(",", "");
            try {
                data.put("subtotal", new BigDecimal(subtotalStr));
            } catch (NumberFormatException e) {
                log.warn("Failed to parse subtotal: {}", subtotalStr);
            }
        }
        
        // Extract tax
        Pattern taxPattern = Pattern.compile(
            "tax[:\\s]*\\$?([0-9,]+\\.?[0-9]{0,2})",
            Pattern.CASE_INSENSITIVE
        );
        Matcher taxMatcher = taxPattern.matcher(ocrText);
        if (taxMatcher.find()) {
            String taxStr = taxMatcher.group(1).replace(",", "");
            try {
                data.put("tax", new BigDecimal(taxStr));
            } catch (NumberFormatException e) {
                log.warn("Failed to parse tax: {}", taxStr);
            }
        }
        
        // Extract date
        LocalDate date = extractDate(ocrText);
        if (date != null) {
            data.put("date", date.toString());
        }
        
        // Extract payment method
        if (ocrText.toLowerCase().contains("cash")) {
            data.put("paymentMethod", "CASH");
        } else if (ocrText.toLowerCase().contains("credit") || ocrText.toLowerCase().contains("visa") || 
                   ocrText.toLowerCase().contains("mastercard")) {
            data.put("paymentMethod", "CREDIT_CARD");
        } else if (ocrText.toLowerCase().contains("debit")) {
            data.put("paymentMethod", "DEBIT_CARD");
        }
        
        data.put("currency", "USD");
        
        log.debug("Extracted receipt data: {}", data);
        return data;
    }

    @Override
    public Map<String, Object> extractInvoiceData(String ocrText) {
        log.debug("Extracting invoice data");
        
        Map<String, Object> data = new HashMap<>();
        
        // Extract invoice number
        Pattern invoicePattern = Pattern.compile(
            "invoice\\s*#?[:\\s]*([A-Z0-9-]+)",
            Pattern.CASE_INSENSITIVE
        );
        Matcher invoiceMatcher = invoicePattern.matcher(ocrText);
        if (invoiceMatcher.find()) {
            data.put("invoiceNumber", invoiceMatcher.group(1));
        }
        
        // Extract vendor (look for "from:" or first line)
        Pattern vendorPattern = Pattern.compile(
            "from[:\\s]*(.+)",
            Pattern.CASE_INSENSITIVE
        );
        Matcher vendorMatcher = vendorPattern.matcher(ocrText);
        if (vendorMatcher.find()) {
            data.put("vendorName", vendorMatcher.group(1).trim());
        }
        
        // Extract total
        Pattern totalPattern = Pattern.compile(
            "total[:\\s]*\\$?([0-9,]+\\.?[0-9]{0,2})",
            Pattern.CASE_INSENSITIVE
        );
        Matcher totalMatcher = totalPattern.matcher(ocrText);
        if (totalMatcher.find()) {
            String totalStr = totalMatcher.group(1).replace(",", "");
            try {
                data.put("total", new BigDecimal(totalStr));
            } catch (NumberFormatException e) {
                log.warn("Failed to parse total: {}", totalStr);
            }
        }
        
        // Extract invoice date
        LocalDate invoiceDate = extractDate(ocrText);
        if (invoiceDate != null) {
            data.put("invoiceDate", invoiceDate.toString());
        }
        
        // Extract due date
        Pattern dueDatePattern = Pattern.compile(
            "due[:\\s]*(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})",
            Pattern.CASE_INSENSITIVE
        );
        Matcher dueDateMatcher = dueDatePattern.matcher(ocrText);
        if (dueDateMatcher.find()) {
            LocalDate dueDate = parseDate(dueDateMatcher.group(1));
            if (dueDate != null) {
                data.put("dueDate", dueDate.toString());
            }
        }
        
        data.put("currency", "USD");
        
        log.debug("Extracted invoice data: {}", data);
        return data;
    }

    @Override
    public Map<String, Object> extractCheckData(String ocrText) {
        log.debug("Extracting check data");
        
        Map<String, Object> data = new HashMap<>();
        
        // Extract routing number (9 digits)
        Pattern routingPattern = Pattern.compile("\\b([0-9]{9})\\b");
        Matcher routingMatcher = routingPattern.matcher(ocrText);
        if (routingMatcher.find()) {
            data.put("routingNumber", routingMatcher.group(1));
        }
        
        // Extract account number (8-17 digits)
        Pattern accountPattern = Pattern.compile("\\b([0-9]{8,17})\\b");
        Matcher accountMatcher = accountPattern.matcher(ocrText);
        if (accountMatcher.find()) {
            String accountNumber = accountMatcher.group(1);
            // Mask account number (show last 4)
            if (accountNumber.length() > 4) {
                String masked = "****" + accountNumber.substring(accountNumber.length() - 4);
                data.put("accountNumber", masked);
            } else {
                data.put("accountNumber", accountNumber);
            }
        }
        
        // Extract check number
        Pattern checkNumPattern = Pattern.compile(
            "check\\s*#?[:\\s]*([0-9]+)",
            Pattern.CASE_INSENSITIVE
        );
        Matcher checkNumMatcher = checkNumPattern.matcher(ocrText);
        if (checkNumMatcher.find()) {
            data.put("checkNumber", checkNumMatcher.group(1));
        }
        
        // Extract amount
        Pattern amountPattern = Pattern.compile("\\$([0-9,]+\\.[0-9]{2})");
        Matcher amountMatcher = amountPattern.matcher(ocrText);
        if (amountMatcher.find()) {
            String amountStr = amountMatcher.group(1).replace(",", "");
            try {
                data.put("amount", new BigDecimal(amountStr));
            } catch (NumberFormatException e) {
                log.warn("Failed to parse amount: {}", amountStr);
            }
        }
        
        // Extract payee
        Pattern payeePattern = Pattern.compile(
            "pay to the order of[:\\s]*(.+)",
            Pattern.CASE_INSENSITIVE
        );
        Matcher payeeMatcher = payeePattern.matcher(ocrText);
        if (payeeMatcher.find()) {
            data.put("payee", payeeMatcher.group(1).trim());
        }
        
        // Extract date
        LocalDate date = extractDate(ocrText);
        if (date != null) {
            data.put("date", date.toString());
        }
        
        data.put("currency", "USD");
        
        log.debug("Extracted check data: {}", data);
        return data;
    }

    @Override
    public Map<String, Object> extractBankStatementData(String ocrText) {
        log.debug("Extracting bank statement data");
        
        Map<String, Object> data = new HashMap<>();
        
        // Extract account number
        Pattern accountPattern = Pattern.compile(
            "account\\s*#?[:\\s]*([0-9X-]+)",
            Pattern.CASE_INSENSITIVE
        );
        Matcher accountMatcher = accountPattern.matcher(ocrText);
        if (accountMatcher.find()) {
            data.put("accountNumber", accountMatcher.group(1));
        }
        
        // Extract statement date
        Pattern statementDatePattern = Pattern.compile(
            "statement date[:\\s]*(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})",
            Pattern.CASE_INSENSITIVE
        );
        Matcher statementDateMatcher = statementDatePattern.matcher(ocrText);
        if (statementDateMatcher.find()) {
            LocalDate statementDate = parseDate(statementDateMatcher.group(1));
            if (statementDate != null) {
                data.put("statementDate", statementDate.toString());
            }
        }
        
        // Extract beginning balance
        Pattern beginningBalancePattern = Pattern.compile(
            "beginning balance[:\\s]*\\$?([0-9,]+\\.?[0-9]{0,2})",
            Pattern.CASE_INSENSITIVE
        );
        Matcher beginningBalanceMatcher = beginningBalancePattern.matcher(ocrText);
        if (beginningBalanceMatcher.find()) {
            String balanceStr = beginningBalanceMatcher.group(1).replace(",", "");
            try {
                data.put("beginningBalance", new BigDecimal(balanceStr));
            } catch (NumberFormatException e) {
                log.warn("Failed to parse beginning balance: {}", balanceStr);
            }
        }
        
        // Extract ending balance
        Pattern endingBalancePattern = Pattern.compile(
            "ending balance[:\\s]*\\$?([0-9,]+\\.?[0-9]{0,2})",
            Pattern.CASE_INSENSITIVE
        );
        Matcher endingBalanceMatcher = endingBalancePattern.matcher(ocrText);
        if (endingBalanceMatcher.find()) {
            String balanceStr = endingBalanceMatcher.group(1).replace(",", "");
            try {
                data.put("endingBalance", new BigDecimal(balanceStr));
            } catch (NumberFormatException e) {
                log.warn("Failed to parse ending balance: {}", balanceStr);
            }
        }
        
        data.put("currency", "USD");
        
        log.debug("Extracted bank statement data: {}", data);
        return data;
    }

    @Override
    public Map<String, Object> extractIdDocumentData(String ocrText) {
        log.debug("Extracting ID document data");
        
        Map<String, Object> data = new HashMap<>();
        
        // Extract name
        Pattern namePattern = Pattern.compile(
            "name[:\\s]*(.+)",
            Pattern.CASE_INSENSITIVE
        );
        Matcher nameMatcher = namePattern.matcher(ocrText);
        if (nameMatcher.find()) {
            data.put("fullName", nameMatcher.group(1).trim());
        }
        
        // Extract date of birth
        Pattern dobPattern = Pattern.compile(
            "(?:dob|date of birth)[:\\s]*(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})",
            Pattern.CASE_INSENSITIVE
        );
        Matcher dobMatcher = dobPattern.matcher(ocrText);
        if (dobMatcher.find()) {
            LocalDate dob = parseDate(dobMatcher.group(1));
            if (dob != null) {
                data.put("dateOfBirth", dob.toString());
            }
        }
        
        // Extract ID number
        Pattern idPattern = Pattern.compile(
            "(?:id|license)\\s*#?[:\\s]*([A-Z0-9-]+)",
            Pattern.CASE_INSENSITIVE
        );
        Matcher idMatcher = idPattern.matcher(ocrText);
        if (idMatcher.find()) {
            data.put("idNumber", idMatcher.group(1));
        }
        
        // Extract expiration date
        Pattern expPattern = Pattern.compile(
            "(?:exp|expires)[:\\s]*(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})",
            Pattern.CASE_INSENSITIVE
        );
        Matcher expMatcher = expPattern.matcher(ocrText);
        if (expMatcher.find()) {
            LocalDate expDate = parseDate(expMatcher.group(1));
            if (expDate != null) {
                data.put("expirationDate", expDate.toString());
            }
        }
        
        log.debug("Extracted ID document data: {}", data);
        return data;
    }

    @Override
    public Map<String, String> validateExtractedData(
        Map<String, Object> extractedData,
        DocumentType documentType
    ) {
        log.debug("Validating extracted data for document type: {}", documentType);
        
        Map<String, String> warnings = new HashMap<>();
        
        // Validate based on document type
        switch (documentType) {
            case RECEIPT -> validateReceiptData(extractedData, warnings);
            case INVOICE -> validateInvoiceData(extractedData, warnings);
            case CHECK -> validateCheckData(extractedData, warnings);
            case BANK_STATEMENT -> validateBankStatementData(extractedData, warnings);
            case ID_DOCUMENT -> validateIdDocumentData(extractedData, warnings);
        }
        
        return warnings;
    }

    /**
     * Extract date from text using multiple patterns.
     */
    private LocalDate extractDate(String text) {
        // Try multiple date patterns
        String[] patterns = {
            "\\d{1,2}[/-]\\d{1,2}[/-]\\d{4}",
            "\\d{1,2}[/-]\\d{1,2}[/-]\\d{2}",
            "\\d{4}[/-]\\d{1,2}[/-]\\d{1,2}"
        };
        
        for (String patternStr : patterns) {
            Pattern pattern = Pattern.compile(patternStr);
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                LocalDate date = parseDate(matcher.group());
                if (date != null) {
                    return date;
                }
            }
        }
        
        return null;
    }

    /**
     * Parse date string to LocalDate.
     */
    private LocalDate parseDate(String dateStr) {
        DateTimeFormatter[] formatters = {
            DateTimeFormatter.ofPattern("M/d/yyyy"),
            DateTimeFormatter.ofPattern("M-d-yyyy"),
            DateTimeFormatter.ofPattern("M/d/yy"),
            DateTimeFormatter.ofPattern("M-d-yy"),
            DateTimeFormatter.ofPattern("yyyy/M/d"),
            DateTimeFormatter.ofPattern("yyyy-M-d")
        };
        
        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDate.parse(dateStr, formatter);
            } catch (DateTimeParseException e) {
                // Try next formatter
            }
        }
        
        return null;
    }

    private void validateReceiptData(Map<String, Object> data, Map<String, String> warnings) {
        if (!data.containsKey("merchant")) {
            warnings.put("merchant", "Merchant name not found");
        }
        if (!data.containsKey("total")) {
            warnings.put("total", "Total amount not found");
        }
    }

    private void validateInvoiceData(Map<String, Object> data, Map<String, String> warnings) {
        if (!data.containsKey("invoiceNumber")) {
            warnings.put("invoiceNumber", "Invoice number not found");
        }
        if (!data.containsKey("vendorName")) {
            warnings.put("vendorName", "Vendor name not found");
        }
        if (!data.containsKey("total")) {
            warnings.put("total", "Total amount not found");
        }
    }

    private void validateCheckData(Map<String, Object> data, Map<String, String> warnings) {
        if (!data.containsKey("routingNumber")) {
            warnings.put("routingNumber", "Routing number not found");
        }
        if (!data.containsKey("accountNumber")) {
            warnings.put("accountNumber", "Account number not found");
        }
        if (!data.containsKey("amount")) {
            warnings.put("amount", "Amount not found");
        }
    }

    private void validateBankStatementData(Map<String, Object> data, Map<String, String> warnings) {
        if (!data.containsKey("accountNumber")) {
            warnings.put("accountNumber", "Account number not found");
        }
        if (!data.containsKey("statementDate")) {
            warnings.put("statementDate", "Statement date not found");
        }
    }

    private void validateIdDocumentData(Map<String, Object> data, Map<String, String> warnings) {
        if (!data.containsKey("fullName")) {
            warnings.put("fullName", "Full name not found");
        }
        if (!data.containsKey("dateOfBirth")) {
            warnings.put("dateOfBirth", "Date of birth not found");
        }
        if (!data.containsKey("idNumber")) {
            warnings.put("idNumber", "ID number not found");
        }
    }
}
