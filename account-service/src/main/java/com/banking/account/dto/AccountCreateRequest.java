package com.banking.account.dto;

import com.banking.account.domain.AccountType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Account Creation Request DTO
 * 
 * Request payload for creating a new account.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountCreateRequest {

    @NotNull(message = "Account type is required")
    private AccountType accountType;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters (ISO 4217)")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be uppercase ISO 4217 code")
    private String currency;

    @DecimalMin(value = "0.00", message = "Initial deposit must be non-negative")
    @Digits(integer = 17, fraction = 2, message = "Invalid amount format")
    private BigDecimal initialDeposit;

    @DecimalMin(value = "0.00", message = "Overdraft limit must be non-negative")
    @Digits(integer = 17, fraction = 2, message = "Invalid overdraft limit format")
    private BigDecimal overdraftLimit;

    @DecimalMin(value = "0.0000", message = "Interest rate must be non-negative")
    @DecimalMax(value = "100.0000", message = "Interest rate cannot exceed 100%")
    @Digits(integer = 3, fraction = 4, message = "Invalid interest rate format")
    private BigDecimal interestRate;
}
