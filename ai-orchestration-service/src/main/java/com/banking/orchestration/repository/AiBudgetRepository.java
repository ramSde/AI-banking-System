package com.banking.orchestration.repository;

import com.banking.orchestration.domain.AiBudget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AiBudgetRepository extends JpaRepository<AiBudget, UUID> {

    @Query("SELECT ab FROM AiBudget ab WHERE ab.userId = :userId AND ab.deletedAt IS NULL")
    Optional<AiBudget> findByUserIdAndNotDeleted(@Param("userId") UUID userId);

    @Modifying
    @Transactional
    @Query("UPDATE AiBudget ab SET ab.dailySpentUsd = ab.dailySpentUsd + :amount, ab.updatedAt = :now WHERE ab.userId = :userId")
    void incrementDailySpent(@Param("userId") UUID userId, @Param("amount") BigDecimal amount, @Param("now") Instant now);

    @Modifying
    @Transactional
    @Query("UPDATE AiBudget ab SET ab.monthlySpentUsd = ab.monthlySpentUsd + :amount, ab.updatedAt = :now WHERE ab.userId = :userId")
    void incrementMonthlySpent(@Param("userId") UUID userId, @Param("amount") BigDecimal amount, @Param("now") Instant now);
}
