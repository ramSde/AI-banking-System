package com.banking.transaction.service.impl;

import com.banking.transaction.domain.EntryType;
import com.banking.transaction.domain.LedgerEntry;
import com.banking.transaction.dto.LedgerEntryResponse;
import com.banking.transaction.mapper.TransactionMapper;
import com.banking.transaction.repository.LedgerEntryRepository;
import com.banking.transaction.service.LedgerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Ledger Service Implementation
 * 
 * Implements double-entry bookkeeping for all financial transactions.
 * Every transaction creates at least two ledger entries (debit and credit).
 */
@Service
public class LedgerServiceImpl implements LedgerService {

    private static final Logger log = LoggerFactory.getLogger(LedgerServiceImpl.class);

    private final LedgerEntryRepository ledgerEntryRepository;
    private final TransactionMapper transactionMapper;

    public LedgerServiceImpl(
            LedgerEntryRepository ledgerEntryRepository,
            TransactionMapper transactionMapper) {
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.transactionMapper = transactionMapper;
    }

    @Override
    @Transactional
    public void createLedgerEntries(UUID transactionId, UUID sourceAccountId, UUID destinationAccountId,
                                    BigDecimal amount, String currency, String description,
                                    BigDecimal sourceBalanceBefore, BigDecimal destBalanceBefore) {
        log.info("Creating ledger entries for transaction: {}", transactionId);

        if (sourceAccountId != null) {
            BigDecimal sourceBalanceAfter = sourceBalanceBefore.subtract(amount);
            LedgerEntry debitEntry = LedgerEntry.builder()
                    .transactionId(transactionId)
                    .accountId(sourceAccountId)
                    .entryType(EntryType.DEBIT)
                    .amount(amount)
                    .currency(currency)
                    .balanceBefore(sourceBalanceBefore)
                    .balanceAfter(sourceBalanceAfter)
                    .description(description)
                    .build();
            ledgerEntryRepository.save(debitEntry);
            log.debug("Created debit entry for account: {}", sourceAccountId);
        }

        if (destinationAccountId != null) {
            BigDecimal destBalanceAfter = destBalanceBefore.add(amount);
            LedgerEntry creditEntry = LedgerEntry.builder()
                    .transactionId(transactionId)
                    .accountId(destinationAccountId)
                    .entryType(EntryType.CREDIT)
                    .amount(amount)
                    .currency(currency)
                    .balanceBefore(destBalanceBefore)
                    .balanceAfter(destBalanceAfter)
                    .description(description)
                    .build();
            ledgerEntryRepository.save(creditEntry);
            log.debug("Created credit entry for account: {}", destinationAccountId);
        }

        log.info("Ledger entries created successfully for transaction: {}", transactionId);
    }

    @Override
    public List<LedgerEntryResponse> getLedgerEntriesByTransactionId(UUID transactionId) {
        log.debug("Fetching ledger entries for transaction: {}", transactionId);
        return ledgerEntryRepository.findByTransactionId(transactionId)
                .stream()
                .map(transactionMapper::toLedgerEntryResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<LedgerEntryResponse> getLedgerEntriesByAccountId(UUID accountId) {
        log.debug("Fetching ledger entries for account: {}", accountId);
        return ledgerEntryRepository.findByAccountId(accountId)
                .stream()
                .map(transactionMapper::toLedgerEntryResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<LedgerEntryResponse> getLedgerEntriesByAccountIdAndDateRange(UUID accountId, Instant startDate, Instant endDate) {
        log.debug("Fetching ledger entries for account: {} between {} and {}", accountId, startDate, endDate);
        return ledgerEntryRepository.findByAccountIdAndDateRange(accountId, startDate, endDate)
                .stream()
                .map(transactionMapper::toLedgerEntryResponse)
                .collect(Collectors.toList());
    }
}
