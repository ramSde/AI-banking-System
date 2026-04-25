package com.banking.account.repository;

import com.banking.account.domain.AccountBalanceHistory;
import com.banking.account.domain.BalanceChangeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Account Balance History Repository
 * 
 * Data access layer for AccountBalanceHistory entity.
 * This is an immutable audit log - no update or delete operations.
 */
@Repository
public interface AccountBalanceHistoryRepository extends JpaRepository<AccountBalanceHistory, UUID> {

    /**
     * Find all balance history for an account
     */
    @Query("SELECT h FROM AccountBalanceHistory h WHERE h.accountId = :accountId ORDER BY h.performedAt DESC")
    Page<AccountBalanceHistory> findByAccountId(@Param("accountId") UUID accountId, Pageable pageable);

    /**
     * Find balance history by transaction ID
     */
    @Query("SELECT h FROM AccountBalanceHistory h WHERE h.transactionId = :transactionId ORDER BY h.performedAt DESC")
    List<AccountBalanceHistory> findByTransactionId(@Param("transactionId") UUID transactionId);

    /**
     * Find balance history by account and date range
     */
    @Query("SELECT h FROM AccountBalanceHistory h WHERE h.accountId = :accountId AND h.performedAt BETWEEN :startDate AND :endDate ORDER BY h.performedAt DESC")
    Page<AccountBalanceHistory> findByAccountIdAndDateRange(
            @Param("accountId") UUID accountId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            Pageable pageable
    );

    /**
     * Find balance history by account and change type
     */
    @Query("SELECT h FROM AccountBalanceHistory h WHERE h.accountId = :accountId AND h.changeType = :changeType ORDER BY h.performedAt DESC")
    Page<AccountBalanceHistory> findByAccountIdAndChangeType(
            @Param("accountId") UUID accountId,
            @Param("changeType") BalanceChangeType changeType,
            Pageable pageable
    );

    /**
     * Count balance changes for an account
     */
    @Query("SELECT COUNT(h) FROM AccountBalanceHistory h WHERE h.accountId = :accountId")
    long countByAccountId(@Param("accountId") UUID accountId);
}
