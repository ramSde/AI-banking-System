package com.banking.transaction.repository;

import com.banking.transaction.domain.LedgerEntry;
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
 * Ledger Entry Repository
 * 
 * Data access layer for LedgerEntry entity.
 * Provides queries for transaction audit trail and reconciliation.
 */
@Repository
public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {

    @Query("SELECT l FROM LedgerEntry l WHERE l.transactionId = :transactionId ORDER BY l.createdAt ASC")
    List<LedgerEntry> findByTransactionId(@Param("transactionId") UUID transactionId);

    @Query("SELECT l FROM LedgerEntry l WHERE l.accountId = :accountId ORDER BY l.createdAt DESC")
    List<LedgerEntry> findByAccountId(@Param("accountId") UUID accountId);

    @Query("SELECT l FROM LedgerEntry l WHERE l.accountId = :accountId")
    Page<LedgerEntry> findByAccountId(@Param("accountId") UUID accountId, Pageable pageable);

    @Query("SELECT l FROM LedgerEntry l WHERE l.accountId = :accountId AND l.createdAt BETWEEN :startDate AND :endDate ORDER BY l.createdAt DESC")
    List<LedgerEntry> findByAccountIdAndDateRange(
            @Param("accountId") UUID accountId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );

    @Query("SELECT l FROM LedgerEntry l WHERE l.createdAt BETWEEN :startDate AND :endDate ORDER BY l.createdAt DESC")
    Page<LedgerEntry> findByDateRange(
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            Pageable pageable
    );

    @Query("SELECT COUNT(l) FROM LedgerEntry l WHERE l.transactionId = :transactionId")
    long countByTransactionId(@Param("transactionId") UUID transactionId);
}
