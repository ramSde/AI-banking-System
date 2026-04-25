package com.banking.transaction.repository;

import com.banking.transaction.domain.HoldType;
import com.banking.transaction.domain.TransactionHold;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Transaction Hold Repository
 * 
 * Data access layer for TransactionHold entity.
 * Manages authorization holds and reservations.
 */
@Repository
public interface TransactionHoldRepository extends JpaRepository<TransactionHold, UUID> {

    @Query("SELECT h FROM TransactionHold h WHERE h.holdReference = :reference")
    Optional<TransactionHold> findByHoldReference(@Param("reference") String reference);

    @Query("SELECT h FROM TransactionHold h WHERE h.accountId = :accountId AND h.releasedAt IS NULL AND h.capturedTransactionId IS NULL ORDER BY h.createdAt DESC")
    List<TransactionHold> findActiveByAccountId(@Param("accountId") UUID accountId);

    @Query("SELECT h FROM TransactionHold h WHERE h.accountId = :accountId")
    Page<TransactionHold> findByAccountId(@Param("accountId") UUID accountId, Pageable pageable);

    @Query("SELECT h FROM TransactionHold h WHERE h.holdType = :holdType AND h.releasedAt IS NULL AND h.capturedTransactionId IS NULL")
    Page<TransactionHold> findActiveByHoldType(@Param("holdType") HoldType holdType, Pageable pageable);

    @Query("SELECT h FROM TransactionHold h WHERE h.expiresAt < :now AND h.releasedAt IS NULL AND h.capturedTransactionId IS NULL")
    List<TransactionHold> findExpiredHolds(@Param("now") Instant now);

    @Query("SELECT h FROM TransactionHold h WHERE h.initiatedBy = :userId")
    Page<TransactionHold> findByInitiatedBy(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT CASE WHEN COUNT(h) > 0 THEN true ELSE false END FROM TransactionHold h WHERE h.holdReference = :reference")
    boolean existsByHoldReference(@Param("reference") String reference);

    @Query("SELECT SUM(h.amount) FROM TransactionHold h WHERE h.accountId = :accountId AND h.releasedAt IS NULL AND h.capturedTransactionId IS NULL AND h.expiresAt > :now")
    java.math.BigDecimal sumActiveHoldsByAccountId(@Param("accountId") UUID accountId, @Param("now") Instant now);
}
