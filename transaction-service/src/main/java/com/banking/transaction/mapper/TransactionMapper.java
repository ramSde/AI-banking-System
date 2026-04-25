package com.banking.transaction.mapper;

import com.banking.transaction.domain.LedgerEntry;
import com.banking.transaction.domain.Transaction;
import com.banking.transaction.domain.TransactionHold;
import com.banking.transaction.dto.LedgerEntryResponse;
import com.banking.transaction.dto.TransactionHoldResponse;
import com.banking.transaction.dto.TransactionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

/**
 * Transaction Mapper
 * 
 * MapStruct mapper for converting between domain entities and DTOs.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TransactionMapper {

    TransactionResponse toTransactionResponse(Transaction transaction);

    LedgerEntryResponse toLedgerEntryResponse(LedgerEntry ledgerEntry);

    TransactionHoldResponse toTransactionHoldResponse(TransactionHold transactionHold);
}
