package com.banking.account.service.impl;

import com.banking.account.config.AccountProperties;
import com.banking.account.domain.Account;
import com.banking.account.domain.AccountBalanceHistory;
import com.banking.account.domain.AccountStatus;
import com.banking.account.domain.AccountType;
import com.banking.account.domain.BalanceChangeType;
import com.banking.account.dto.AccountCreateRequest;
import com.banking.account.dto.AccountUpdateRequest;
import com.banking.account.event.*;
import com.banking.account.exception.*;
import com.banking.account.repository.AccountBalanceHistoryRepository;
import com.banking.account.repository.AccountRepository;
import com.banking.account.service.AccountService;
import com.banking.account.util.AccountNumberGenerator;
import com.banking.account.util.IbanGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Account Service Implementation
 * 
 * Implements business logic for account management with caching and event publishing.
 */
@Service
@Transactional
public class AccountServiceImpl implements AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountServiceImpl.class);

    private final AccountRepository accountRepository;
    private final AccountBalanceHistoryRepository balanceHistoryRepository;
    private final AccountNumberGenerator accountNumberGenerator;
    private final IbanGenerator ibanGenerator;
    private final AccountEventPublisher eventPublisher;
    private final AccountProperties accountProperties;

    public AccountServiceImpl(
            AccountRepository accountRepository,
            AccountBalanceHistoryRepository balanceHistoryRepository,
            AccountNumberGenerator accountNumberGenerator,
            IbanGenerator ibanGenerator,
            AccountEventPublisher eventPublisher,
            AccountProperties accountProperties
    ) {
        this.accountRepository = accountRepository;
        this.balanceHistoryRepository = balanceHistoryRepository;
        this.accountNumberGenerator = accountNumberGenerator;
        this.ibanGenerator = ibanGenerator;
        this.eventPublisher = eventPublisher;
        this.accountProperties = accountProperties;
    }

    @Override
    @CacheEvict(value = "account:list", key = "#userId")
    public Account createAccount(UUID userId, AccountCreateRequest request) {
        log.info("Creating account for user: {}, type: {}", userId, request.getAccountType());

        if (!canCreateAccount(userId)) {
            throw new MaxAccountsExceededException(accountProperties.getMaxAccountsPerUser());
        }

        String accountNumber = generateUniqueAccountNumber();
        String iban = ibanGenerator.generate(
                accountProperties.getIban().getCountryCode(),
                accountProperties.getIban().getBankCode(),
                accountNumber
        );

        BigDecimal initialDeposit = request.getInitialDeposit() != null 
                ? request.getInitialDeposit().setScale(2, RoundingMode.HALF_UP) 
                : BigDecimal.ZERO;

        validateMinimumBalance(request.getAccountType(), initialDeposit);

        Account account = Account.builder()
                .userId(userId)
                .accountNumber(accountNumber)
                .iban(iban)
                .accountType(request.getAccountType())
                .accountStatus(AccountStatus.ACTIVE)
                .currency(request.getCurrency() != null ? request.getCurrency() : accountProperties.getDefaultCurrency())
                .balance(initialDeposit)
                .availableBalance(initialDeposit)
                .holdBalance(BigDecimal.ZERO)
                .overdraftLimit(request.getOverdraftLimit() != null ? request.getOverdraftLimit() : BigDecimal.ZERO)
                .interestRate(request.getInterestRate() != null ? request.getInterestRate() : BigDecimal.ZERO)
                .openedAt(Instant.now())
                .build();

        account = accountRepository.save(account);
        log.info("Account created successfully: {}", accountNumber);

        if (initialDeposit.compareTo(BigDecimal.ZERO) > 0) {
            recordBalanceChange(account.getId(), BigDecimal.ZERO, initialDeposit, 
                    initialDeposit, BalanceChangeType.CREDIT, null, 
                    "Initial deposit", userId);
        }

        AccountCreatedEvent event = AccountCreatedEvent.create(
                account.getId(),
                account.getUserId(),
                account.getAccountNumber(),
                account.getIban(),
                account.getAccountType(),
                account.getCurrency(),
                initialDeposit,
                account.getOpenedAt()
        );
        eventPublisher.publishAccountCreated(event);

        return account;
    }

    @Override
    @Cacheable(value = "account:details", key = "#accountId")
    @Transactional(readOnly = true)
    public Account getAccountById(UUID accountId) {
        log.debug("Fetching account by ID: {}", accountId);
        return accountRepository.findByIdAndNotDeleted(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }

    @Override
    @Cacheable(value = "account:details", key = "#accountNumber")
    @Transactional(readOnly = true)
    public Account getAccountByAccountNumber(String accountNumber) {
        log.debug("Fetching account by account number: {}", accountNumber);
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));
    }

    @Override
    @Cacheable(value = "account:list", key = "#userId")
    @Transactional(readOnly = true)
    public List<Account> getAccountsByUserId(UUID userId) {
        log.debug("Fetching all accounts for user: {}", userId);
        return accountRepository.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Account> getAccountsByUserId(UUID userId, Pageable pageable) {
        log.debug("Fetching accounts for user: {} with pagination", userId);
        return accountRepository.findByUserId(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Account> getAccountsByUserIdAndStatus(UUID userId, AccountStatus status) {
        log.debug("Fetching accounts for user: {} with status: {}", userId, status);
        return accountRepository.findByUserIdAndStatus(userId, status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Account> getAccountsByUserIdAndType(UUID userId, AccountType type) {
        log.debug("Fetching accounts for user: {} with type: {}", userId, type);
        return accountRepository.findByUserIdAndType(userId, type);
    }

    @Override
    @CacheEvict(value = {"account:details", "account:list"}, key = "#accountId")
    public Account updateAccount(UUID accountId, AccountUpdateRequest request) {
        log.info("Updating account: {}", accountId);

        Account account = getAccountById(accountId);

        if (account.isClosed()) {
            throw new AccountClosedException(accountId);
        }

        boolean updated = false;

        if (request.getOverdraftLimit() != null) {
            account.setOverdraftLimit(request.getOverdraftLimit().setScale(2, RoundingMode.HALF_UP));
            updated = true;
        }

        if (request.getInterestRate() != null) {
            account.setInterestRate(request.getInterestRate());
            updated = true;
        }

        if (updated) {
            account = accountRepository.save(account);
            log.info("Account updated successfully: {}", account.getAccountNumber());

            AccountUpdatedEvent event = AccountUpdatedEvent.create(
                    account.getId(),
                    account.getUserId(),
                    account.getAccountNumber(),
                    account.getOverdraftLimit(),
                    account.getInterestRate()
            );
            eventPublisher.publishAccountUpdated(event);
        }

        return account;
    }

    @Override
    @CacheEvict(value = {"account:details", "account:list"}, key = "#accountId")
    public Account updateAccountStatus(UUID accountId, AccountStatus newStatus, String reason, UUID changedBy) {
        log.info("Updating account status: {} to {}", accountId, newStatus);

        Account account = getAccountById(accountId);
        AccountStatus previousStatus = account.getAccountStatus();

        if (previousStatus == newStatus) {
            log.warn("Account already has status: {}", newStatus);
            return account;
        }

        account.setAccountStatus(newStatus);

        if (newStatus == AccountStatus.CLOSED) {
            account.setClosedAt(Instant.now());
        }

        account = accountRepository.save(account);
        log.info("Account status updated: {} -> {}", previousStatus, newStatus);

        AccountStatusChangedEvent event = AccountStatusChangedEvent.create(
                account.getId(),
                account.getUserId(),
                account.getAccountNumber(),
                previousStatus,
                newStatus,
                reason,
                changedBy
        );
        eventPublisher.publishAccountStatusChanged(event);

        return account;
    }

    @Override
    @CacheEvict(value = {"account:details", "account:list"}, key = "#accountId")
    public Account freezeAccount(UUID accountId, String reason, UUID frozenBy) {
        log.info("Freezing account: {}", accountId);
        return updateAccountStatus(accountId, AccountStatus.FROZEN, reason, frozenBy);
    }

    @Override
    @CacheEvict(value = {"account:details", "account:list"}, key = "#accountId")
    public Account unfreezeAccount(UUID accountId, UUID unfrozenBy) {
        log.info("Unfreezing account: {}", accountId);
        return updateAccountStatus(accountId, AccountStatus.ACTIVE, "Account unfrozen", unfrozenBy);
    }

    @Override
    @CacheEvict(value = {"account:details", "account:list"}, key = "#accountId")
    public Account closeAccount(UUID accountId, String reason, UUID closedBy) {
        log.info("Closing account: {}", accountId);

        Account account = getAccountById(accountId);

        if (account.isClosed()) {
            throw new AccountClosedException(accountId);
        }

        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new InvalidAccountOperationException(
                    "Cannot close account with non-zero balance. Current balance: " + account.getBalance()
            );
        }

        account = updateAccountStatus(accountId, AccountStatus.CLOSED, reason, closedBy);

        AccountClosedEvent event = AccountClosedEvent.create(
                account.getId(),
                account.getUserId(),
                account.getAccountNumber(),
                account.getBalance(),
                reason,
                closedBy
        );
        eventPublisher.publishAccountClosed(event);

        return account;
    }

    @Override
    @CacheEvict(value = {"account:details", "account:list"}, key = "#accountId")
    public void deleteAccount(UUID accountId) {
        log.info("Soft deleting account: {}", accountId);

        Account account = getAccountById(accountId);

        if (!account.isClosed()) {
            throw new InvalidAccountOperationException("Account must be closed before deletion");
        }

        account.setDeletedAt(Instant.now());
        accountRepository.save(account);
        log.info("Account soft deleted: {}", account.getAccountNumber());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Account> getAllAccounts(Pageable pageable) {
        log.debug("Fetching all accounts with pagination");
        return accountRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Account> getAccountsByStatus(AccountStatus status, Pageable pageable) {
        log.debug("Fetching accounts by status: {}", status);
        return accountRepository.findByStatus(status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Account> getAccountsByType(AccountType type, Pageable pageable) {
        log.debug("Fetching accounts by type: {}", type);
        return accountRepository.findByType(type, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canCreateAccount(UUID userId) {
        long activeAccounts = countActiveAccounts(userId);
        return activeAccounts < accountProperties.getMaxAccountsPerUser();
    }

    @Override
    @Transactional(readOnly = true)
    public long countActiveAccounts(UUID userId) {
        return accountRepository.countActiveAccountsByUserId(userId);
    }

    private String generateUniqueAccountNumber() {
        String accountNumber;
        int attempts = 0;
        int maxAttempts = 10;

        do {
            accountNumber = accountNumberGenerator.generate(accountProperties.getNumber().getPrefix());
            attempts++;

            if (attempts >= maxAttempts) {
                throw new InvalidAccountOperationException("Failed to generate unique account number after " + maxAttempts + " attempts");
            }
        } while (accountRepository.existsByAccountNumber(accountNumber));

        return accountNumber;
    }

    private void validateMinimumBalance(AccountType accountType, BigDecimal initialDeposit) {
        BigDecimal minBalance = switch (accountType) {
            case SAVINGS -> accountProperties.getMinBalance().getSavings();
            case CHECKING -> accountProperties.getMinBalance().getChecking();
            case CREDIT -> BigDecimal.ZERO;
        };

        if (initialDeposit.compareTo(minBalance) < 0) {
            throw new InvalidAccountOperationException(
                    String.format("Initial deposit must be at least %s for %s account", minBalance, accountType)
            );
        }
    }

    private void recordBalanceChange(UUID accountId, BigDecimal previousBalance, BigDecimal newBalance,
                                     BigDecimal changeAmount, BalanceChangeType changeType,
                                     UUID transactionId, String description, UUID performedBy) {
        AccountBalanceHistory history = AccountBalanceHistory.builder()
                .accountId(accountId)
                .previousBalance(previousBalance)
                .newBalance(newBalance)
                .changeAmount(changeAmount)
                .changeType(changeType)
                .transactionId(transactionId)
                .description(description)
                .performedBy(performedBy)
                .performedAt(Instant.now())
                .build();

        balanceHistoryRepository.save(history);
        log.debug("Balance change recorded for account: {}", accountId);
    }
}
