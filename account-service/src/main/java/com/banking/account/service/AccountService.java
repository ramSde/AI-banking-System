package com.banking.account.service;

import com.banking.account.domain.Account;
import com.banking.account.domain.AccountStatus;
import com.banking.account.domain.AccountType;
import com.banking.account.dto.AccountCreateRequest;
import com.banking.account.dto.AccountUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Account Service Interface
 * 
 * Business logic for account management operations.
 */
public interface AccountService {

    /**
     * Create a new account
     */
    Account createAccount(UUID userId, AccountCreateRequest request);

    /**
     * Get account by ID
     */
    Account getAccountById(UUID accountId);

    /**
     * Get account by account number
     */
    Account getAccountByAccountNumber(String accountNumber);

    /**
     * Get all accounts for a user
     */
    List<Account> getAccountsByUserId(UUID userId);

    /**
     * Get all accounts for a user with pagination
     */
    Page<Account> getAccountsByUserId(UUID userId, Pageable pageable);

    /**
     * Get accounts by user and status
     */
    List<Account> getAccountsByUserIdAndStatus(UUID userId, AccountStatus status);

    /**
     * Get accounts by user and type
     */
    List<Account> getAccountsByUserIdAndType(UUID userId, AccountType type);

    /**
     * Update account settings
     */
    Account updateAccount(UUID accountId, AccountUpdateRequest request);

    /**
     * Update account status (admin only)
     */
    Account updateAccountStatus(UUID accountId, AccountStatus newStatus, String reason, UUID changedBy);

    /**
     * Freeze account (admin only)
     */
    Account freezeAccount(UUID accountId, String reason, UUID frozenBy);

    /**
     * Unfreeze account (admin only)
     */
    Account unfreezeAccount(UUID accountId, UUID unfrozenBy);

    /**
     * Close account
     */
    Account closeAccount(UUID accountId, String reason, UUID closedBy);

    /**
     * Delete account (soft delete)
     */
    void deleteAccount(UUID accountId);

    /**
     * Get all accounts (admin only)
     */
    Page<Account> getAllAccounts(Pageable pageable);

    /**
     * Get accounts by status (admin only)
     */
    Page<Account> getAccountsByStatus(AccountStatus status, Pageable pageable);

    /**
     * Get accounts by type (admin only)
     */
    Page<Account> getAccountsByType(AccountType type, Pageable pageable);

    /**
     * Check if user can create more accounts
     */
    boolean canCreateAccount(UUID userId);

    /**
     * Count active accounts for user
     */
    long countActiveAccounts(UUID userId);
}
