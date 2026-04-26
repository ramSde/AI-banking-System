package com.banking.orchestration.repository;

import com.banking.orchestration.domain.AiUsage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AiUsageRepository extends JpaRepository<AiUsage, UUID> {

    @Query("SELECT au FROM AiUsage au WHERE au.id = :id AND au.deletedAt IS NULL")
    Optional<AiUsage> findByIdAndNotDeleted(@Param("id") UUID id);

    @Query("SELECT au FROM AiUsage au WHERE au.userId = :userId AND au.deletedAt IS NULL ORDER BY au.createdAt DESC")
    Page<AiUsage> findByUserIdAndNotDeleted(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT au FROM AiUsage au WHERE au.userId = :userId AND au.createdAt BETWEEN :startDate AND :endDate AND au.deletedAt IS NULL ORDER BY au.createdAt DESC")
    Page<AiUsage> findByUserIdAndDateRangeAndNotDeleted(
            @Param("userId") UUID userId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            Pageable pageable
    );

    @Query("SELECT SUM(au.totalTokens) FROM AiUsage au WHERE au.userId = :userId AND au.createdAt >= :since AND au.deletedAt IS NULL")
    Long getTotalTokensByUserSince(@Param("userId") UUID userId, @Param("since") Instant since);

    @Query("SELECT SUM(au.costUsd) FROM AiUsage au WHERE au.userId = :userId AND au.createdAt >= :since AND au.deletedAt IS NULL")
    java.math.BigDecimal getTotalCostByUserSince(@Param("userId") UUID userId, @Param("since") Instant since);

    @Query("SELECT au FROM AiUsage au WHERE au.deletedAt IS NULL ORDER BY au.createdAt DESC")
    Page<AiUsage> findAllNotDeleted(Pageable pageable);
}
