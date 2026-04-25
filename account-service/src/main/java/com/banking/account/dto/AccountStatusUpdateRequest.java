package com.banking.account.dto;

import com.banking.account.domain.AccountStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Account Status Update Request DTO
 * 
 * Request payload for updating account status (admin only).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountStatusUpdateRequest {

    @NotNull(message = "Account status is required")
    private AccountStatus accountStatus;

    private String reason;
}
