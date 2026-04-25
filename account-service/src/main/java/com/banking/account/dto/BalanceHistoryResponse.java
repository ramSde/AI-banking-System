package com.banking.account.dto;

import com.banking.account.domain.BalanceChangeType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Balance History Response DTO
 * 
 * Response payload containing balance change history.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BalanceHistoryResponse {

    private UUID id;
    private UUID accountId;
    private BigDecimal previousBalance;
    private BigDecimal newBalance;
    private BigDecimal changeAmount;
    private BalanceChangeType changeType;
    private UUID transactionId;
    private String referenceNumber;
    private String description;
    private UUID performedBy;
    private Instant performedAt;
}
