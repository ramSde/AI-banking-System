package com.banking.vision.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Structured data extracted from checks.
 * 
 * Contains routing number, account number, check number, amount, and payee.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CheckData {

    /**
     * Bank routing number (9 digits).
     */
    private String routingNumber;

    /**
     * Account number (masked for security).
     */
    private String accountNumber;

    /**
     * Check number.
     */
    private String checkNumber;

    /**
     * Check amount (numeric).
     */
    private BigDecimal amount;

    /**
     * Amount in words (as written on check).
     */
    private String amountInWords;

    /**
     * Payee name ("Pay to the order of").
     */
    private String payee;

    /**
     * Check date.
     */
    private LocalDate date;

    /**
     * Memo/note field.
     */
    private String memo;

    /**
     * Payer/account holder name.
     */
    private String payerName;

    /**
     * Payer address.
     */
    private String payerAddress;

    /**
     * Bank name.
     */
    private String bankName;

    /**
     * Currency code (ISO 4217).
     */
    @Builder.Default
    private String currency = "USD";

    /**
     * Check if check is post-dated.
     */
    public boolean isPostDated() {
        return date != null && date.isAfter(LocalDate.now());
    }
}
