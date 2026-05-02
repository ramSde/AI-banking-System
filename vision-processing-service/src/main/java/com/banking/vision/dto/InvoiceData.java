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
 * Structured data extracted from invoices.
 * 
 * Contains vendor information, line items, totals, and payment terms.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InvoiceData {

    /**
     * Invoice number.
     */
    private String invoiceNumber;

    /**
     * Vendor/supplier name.
     */
    private String vendorName;

    /**
     * Vendor address.
     */
    private String vendorAddress;

    /**
     * Vendor tax ID or registration number.
     */
    private String vendorTaxId;

    /**
     * Customer/buyer name.
     */
    private String customerName;

    /**
     * Customer address.
     */
    private String customerAddress;

    /**
     * Invoice date.
     */
    private LocalDate invoiceDate;

    /**
     * Due date for payment.
     */
    private LocalDate dueDate;

    /**
     * Purchase order number (if applicable).
     */
    private String purchaseOrderNumber;

    /**
     * Line items on the invoice.
     */
    private List<LineItem> lineItems;

    /**
     * Subtotal before tax.
     */
    private BigDecimal subtotal;

    /**
     * Tax amount.
     */
    private BigDecimal tax;

    /**
     * Discount amount (if any).
     */
    private BigDecimal discount;

    /**
     * Total amount due.
     */
    private BigDecimal total;

    /**
     * Amount already paid (if partial payment).
     */
    private BigDecimal amountPaid;

    /**
     * Balance due.
     */
    private BigDecimal balanceDue;

    /**
     * Currency code (ISO 4217).
     */
    @Builder.Default
    private String currency = "USD";

    /**
     * Payment terms (e.g., "Net 30", "Due on receipt").
     */
    private String paymentTerms;

    /**
     * Line item on an invoice.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LineItem {
        /**
         * Item description.
         */
        private String description;

        /**
         * Quantity.
         */
        private Integer quantity;

        /**
         * Unit price.
         */
        private BigDecimal unitPrice;

        /**
         * Total price for this line.
         */
        private BigDecimal total;

        /**
         * Item code or SKU (if available).
         */
        private String itemCode;
    }
}
