package com.banking.vision.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Structured data extracted from receipts.
 * 
 * Contains merchant information, line items, totals, and payment details.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReceiptData {

    /**
     * Merchant/store name.
     */
    private String merchant;

    /**
     * Merchant address.
     */
    private String merchantAddress;

    /**
     * Merchant phone number.
     */
    private String merchantPhone;

    /**
     * Transaction date.
     */
    private LocalDate date;

    /**
     * Transaction time (HH:mm format).
     */
    private String time;

    /**
     * Line items purchased.
     */
    private List<LineItem> items;

    /**
     * Subtotal before tax.
     */
    private BigDecimal subtotal;

    /**
     * Tax amount.
     */
    private BigDecimal tax;

    /**
     * Total amount paid.
     */
    private BigDecimal total;

    /**
     * Payment method (CASH, CREDIT_CARD, DEBIT_CARD, etc.).
     */
    private String paymentMethod;

    /**
     * Last 4 digits of card (if card payment).
     */
    private String cardLast4;

    /**
     * Receipt/transaction number.
     */
    private String receiptNumber;

    /**
     * Currency code (ISO 4217).
     */
    @Builder.Default
    private String currency = "USD";

    /**
     * Line item on a receipt.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LineItem {
        /**
         * Item description/name.
         */
        private String description;

        /**
         * Quantity purchased.
         */
        private Integer quantity;

        /**
         * Unit price.
         */
        private BigDecimal unitPrice;

        /**
         * Total price for this line (quantity * unitPrice).
         */
        private BigDecimal total;
    }
}
