package com.banking.account.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Account Update Request DTO
 * 
 * Request payload for updating account settings.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountUpdateRequest {

    @DecimalMin(value = "0.00", message = "Overdraft limit must be non-negative")
    @Digits(integer = 17, fraction = 2, message = "Invalid overdraft limit format")
    private BigDecimal overdraftLimit;

    @DecimalMin(value = "0.0000", message = "Interest rate must be non-negative")
    @DecimalMax(value = "100.0000", message = "Interest rate cannot exceed 100%")
    @Digits(integer = 3, fraction = 4, message = "Invalid interest rate format")
    private BigDecimal interestRate;
}
