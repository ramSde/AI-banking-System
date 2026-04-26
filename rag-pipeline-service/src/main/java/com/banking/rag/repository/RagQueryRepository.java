package com.banking.rag.repository;

import com.banking.rag.domain.RagQuery;
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

@Repository
public interface RagQueryRepository extends JpaRepository<RagQuery, UUID> {

    Optional<RagQuery> findByIdAndDeletedAtIsNull(UUID id);

    Page<RagQuery> findByUserIdAndDeletedAtIsNull(UUID userId, Pageable pageable);

    Page<RagQuery> findBySessionIdAndDeletedAtIsNull(UUID sessionId, Pageable pageable);

    @Query("SELECT rq FROM RagQuery rq WHERE rq.userId = :userId AND rq.deletedAt IS NULL " +
           "AND rq.createdAt BETWEEN :startDate AND :endDate ORDER BY rq.createdAt DESC")
    List<RagQuery> findByUserIdAndDateRange(
            @Param("userId") UUID userId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );

    @Query("SELECT rq FROM RagQuery rq WHERE rq.traceId = :traceId AND rq.deletedAt IS NULL")
    Optional<RagQuery> findByTraceId(@Param("traceId") String traceId);

    @Query("SELECT COUNT(rq) FROM RagQuery rq WHERE rq.userId = :userId AND rq.cacheHit = true AND rq.deletedAt IS NULL")
    Long countCacheHitsByUserId(@Param("userId") UUID userId);

    @Query("SELECT AVG(rq.totalLatencyMs) FROM RagQuery rq WHERE rq.userId = :userId AND rq.deletedAt IS NULL")
    Double getAverageLatencyByUserId(@Param("userId") UUID userId);

    @Query("SELECT rq FROM RagQuery rq WHERE rq.deletedAt IS NULL ORDER BY rq.createdAt DESC")
    Page<RagQuery> findAllActive(Pageable pageable);
}
