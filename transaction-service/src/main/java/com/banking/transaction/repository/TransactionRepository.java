package com.banking.transaction.repository;

import com.banking.transaction.domain.Transaction;
import com.banking.transaction.domain.TransactionStatus;
import com.banking.transaction.domain.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Transaction Repository
 * 
 * Data access layer for Transaction entity with custom queries.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    @Query("SELECT t FROM Transaction t WHERE t.id = :id AND t.deletedAt IS NULL")
    Optional<Transaction> findByIdAndNotDeleted(@Param("id") UUID id);

    @Query("SELECT t FROM Transaction t WHERE t.referenceNumber = :referenceNumber AND t.deletedAt IS NULL")
    Optional<Transaction> findByReferenceNumber(@Param("referenceNumber") String referenceNumber);

    @Query("SELECT t FROM Transaction t WHERE t.idempotencyKey = :idempotencyKey AND t.deletedAt IS NULL")
    Optional<Transaction> findByIdempotencyKey(@Param("idempotencyKey") String idempotencyKey);

    @Query("SELECT t FROM Transaction t WHERE t.initiatedBy = :userId AND t.deletedAt IS NULL ORDER BY t.initiatedAt DESC")
    List<Transaction> findByInitiatedBy(@Param("userId") UUID userId);

    @Query("SELECT t FROM Transaction t WHERE t.initiatedBy = :userId AND t.deletedAt IS NULL")
    Page<Transaction> findByInitiatedBy(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE (t.sourceAccountId = :accountId OR t.destinationAccountId = :accountId) AND t.deletedAt IS NULL ORDER BY t.initiatedAt DESC")
    List<Transaction> findByAccountId(@Param("accountId") UUID accountId);

    @Query("SELECT t FROM Transaction t WHERE (t.sourceAccountId = :accountId OR t.destinationAccountId = :accountId) AND t.deletedAt IS NULL")
    Page<Transaction> findByAccountId(@Param("accountId") UUID accountId, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.transactionStatus = :status AND t.deletedAt IS NULL")
    Page<Transaction> findByStatus(@Param("status") TransactionStatus status, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.transactionType = :type AND t.deletedAt IS NULL")
    Page<Transaction> findByType(@Param("type") TransactionType type, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.initiatedAt BETWEEN :startDate AND :endDate AND t.deletedAt IS NULL ORDER BY t.initiatedAt DESC")
    List<Transaction> findByDateRange(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    @Query("SELECT t FROM Transaction t WHERE t.initiatedBy = :userId AND t.initiatedAt BETWEEN :startDate AND :endDate AND t.deletedAt IS NULL ORDER BY t.initiatedAt DESC")
    List<Transaction> findByUserIdAndDateRange(@Param("userId") UUID userId, @Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    @Query("SELECT t FROM Transaction t WHERE (t.sourceAccountId = :accountId OR t.destinationAccountId = :accountId) AND t.initiatedAt BETWEEN :startDate AND :endDate AND t.deletedAt IS NULL ORDER BY t.initiatedAt DESC")
    List<Transaction> findByAccountIdAndDateRange(@Param("accountId") UUID accountId, @Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM Transaction t WHERE t.referenceNumber = :referenceNumber AND t.deletedAt IS NULL")
    boolean existsByReferenceNumber(@Param("referenceNumber") String referenceNumber);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.initiatedBy = :userId AND t.transactionStatus = 'COMPLETED' AND t.initiatedAt >= :startDate AND t.deletedAt IS NULL")
    BigDecimal sumCompletedTransactionsByUserSince(@Param("userId") UUID userId, @Param("startDate") Instant startDate);
}
