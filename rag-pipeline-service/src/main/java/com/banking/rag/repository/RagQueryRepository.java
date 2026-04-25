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

    @Query("SELECT rq FROM RagQuery rq WHERE rq.id = :id AND rq.deletedAt IS NULL")
    Optional<RagQuery> findByIdAndNotDeleted(@Param("id") UUID id);

    @Query("SELECT rq FROM RagQuery rq WHERE rq.userId = :userId AND rq.deletedAt IS NULL ORDER BY rq.createdAt DESC")
    Page<RagQuery> findByUserIdAndNotDeleted(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT rq FROM RagQuery rq WHERE rq.status = :status AND rq.deletedAt IS NULL ORDER BY rq.createdAt DESC")
    Page<RagQuery> findByStatusAndNotDeleted(@Param("status") String status, Pageable pageable);

    @Query("SELECT rq FROM RagQuery rq WHERE rq.traceId = :traceId AND rq.deletedAt IS NULL")
    Optional<RagQuery> findByTraceIdAndNotDeleted(@Param("traceId") String traceId);

    @Query("SELECT rq FROM RagQuery rq WHERE rq.sessionId = :sessionId AND rq.deletedAt IS NULL ORDER BY rq.createdAt DESC")
    List<RagQuery> findBySessionIdAndNotDeleted(@Param("sessionId") String sessionId);

    @Query("SELECT rq FROM RagQuery rq WHERE rq.userId = :userId AND rq.createdAt BETWEEN :startDate AND :endDate AND rq.deletedAt IS NULL ORDER BY rq.createdAt DESC")
    Page<RagQuery> findByUserIdAndDateRangeAndNotDeleted(
            @Param("userId") UUID userId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            Pageable pageable
    );

    @Query("SELECT COUNT(rq) FROM RagQuery rq WHERE rq.userId = :userId AND rq.status = 'COMPLETED' AND rq.deletedAt IS NULL")
    Long countCompletedQueriesByUserId(@Param("userId") UUID userId);

    @Query("SELECT AVG(rq.totalLatencyMs) FROM RagQuery rq WHERE rq.userId = :userId AND rq.status = 'COMPLETED' AND rq.deletedAt IS NULL")
    Double getAverageLatencyByUserId(@Param("userId") UUID userId);

    @Query("SELECT rq FROM RagQuery rq WHERE rq.deletedAt IS NULL ORDER BY rq.createdAt DESC")
    Page<RagQuery> findAllNotDeleted(Pageable pageable);
}
