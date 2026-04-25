package com.banking.account.mapper;

import com.banking.account.domain.Account;
import com.banking.account.domain.AccountBalanceHistory;
import com.banking.account.dto.AccountResponse;
import com.banking.account.dto.BalanceHistoryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

/**
 * Account Mapper
 * 
 * MapStruct mapper for converting between entities and DTOs.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AccountMapper {

    /**
     * Map Account entity to AccountResponse DTO
     */
    AccountResponse toResponse(Account account);

    /**
     * Map list of Account entities to list of AccountResponse DTOs
     */
    List<AccountResponse> toResponseList(List<Account> accounts);

    /**
     * Map AccountBalanceHistory entity to BalanceHistoryResponse DTO
     */
    BalanceHistoryResponse toBalanceHistoryResponse(AccountBalanceHistory history);

    /**
     * Map list of AccountBalanceHistory entities to list of BalanceHistoryResponse DTOs
     */
    List<BalanceHistoryResponse> toBalanceHistoryResponseList(List<AccountBalanceHistory> histories);
}
