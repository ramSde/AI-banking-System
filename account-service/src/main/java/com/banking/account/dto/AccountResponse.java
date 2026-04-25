package com.banking.account.dto;

import com.banking.account.domain.AccountStatus;
import com.banking.account.domain.AccountType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Account Response DTO
 * 
 * Response payload containing account details.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountResponse {

    private UUID id;
    private UUID userId;
    private String accountNumber;
    private String iban;
    private AccountType accountType;
    private AccountStatus accountStatus;
    private String currency;
    private BigDecimal balance;
    private BigDecimal availableBalance;
    private BigDecimal holdBalance;
    private BigDecimal overdraftLimit;
    private BigDecimal interestRate;
    private Instant openedAt;
    private Instant closedAt;
    private Instant lastTransactionAt;
    private Instant createdAt;
    private Instant updatedAt;
}
