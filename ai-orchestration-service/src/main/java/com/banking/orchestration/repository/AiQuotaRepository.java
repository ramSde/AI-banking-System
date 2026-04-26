package com.banking.orchestration.repository;

import com.banking.orchestration.domain.AiQuota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AiQuotaRepository extends JpaRepository<AiQuota, UUID> {

    @Query("SELECT aq FROM AiQuota aq WHERE aq.userId = :userId AND aq.deletedAt IS NULL")
    Optional<AiQuota> findByUserIdAndNotDeleted(@Param("userId") UUID userId);

    @Modifying
    @Transactional
    @Query("UPDATE AiQuota aq SET aq.dailyTokensUsed = aq.dailyTokensUsed + :tokens, aq.updatedAt = :now WHERE aq.userId = :userId")
    void incrementDailyTokens(@Param("userId") UUID userId, @Param("tokens") Integer tokens, @Param("now") Instant now);

    @Modifying
    @Transactional
    @Query("UPDATE AiQuota aq SET aq.monthlyTokensUsed = aq.monthlyTokensUsed + :tokens, aq.updatedAt = :now WHERE aq.userId = :userId")
    void incrementMonthlyTokens(@Param("userId") UUID userId, @Param("tokens") Integer tokens, @Param("now") Instant now);
}
