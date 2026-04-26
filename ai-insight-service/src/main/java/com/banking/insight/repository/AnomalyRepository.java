package com.banking.insight.repository;

import com.banking.insight.domain.Anomaly;
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
public interface AnomalyRepository extends JpaRepository<Anomaly, UUID> {

    @Query("SELECT a FROM Anomaly a WHERE a.id = :id AND a.deletedAt IS NULL")
    Optional<Anomaly> findByIdAndNotDeleted(@Param("id") UUID id);

    @Query("SELECT a FROM Anomaly a WHERE a.userId = :userId AND a.deletedAt IS NULL ORDER BY a.detectedAt DESC")
    Page<Anomaly> findByUserIdAndNotDeleted(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT a FROM Anomaly a WHERE a.userId = :userId AND a.anomalyType = :type AND a.deletedAt IS NULL ORDER BY a.detectedAt DESC")
    List<Anomaly> findByUserIdAndType(
        @Param("userId") UUID userId,
        @Param("type") Anomaly.AnomalyType type
    );

    @Query("SELECT a FROM Anomaly a WHERE a.userId = :userId AND a.severity = :severity AND a.deletedAt IS NULL ORDER BY a.detectedAt DESC")
    List<Anomaly> findByUserIdAndSeverity(
        @Param("userId") UUID userId,
        @Param("severity") Anomaly.Severity severity
    );

    @Query("SELECT a FROM Anomaly a WHERE a.userId = :userId AND a.isAcknowledged = false AND a.deletedAt IS NULL ORDER BY a.severity DESC, a.detectedAt DESC")
    List<Anomaly> findUnacknowledgedByUserId(@Param("userId") UUID userId);

    @Query("SELECT a FROM Anomaly a WHERE a.userId = :userId AND a.severity IN ('HIGH', 'CRITICAL') AND a.isAcknowledged = false AND a.deletedAt IS NULL ORDER BY a.detectedAt DESC")
    List<Anomaly> findCriticalUnacknowledgedByUserId(@Param("userId") UUID userId);

    @Query("SELECT a FROM Anomaly a WHERE a.userId = :userId AND a.category = :category AND a.deletedAt IS NULL ORDER BY a.detectedAt DESC")
    List<Anomaly> findByUserIdAndCategory(
        @Param("userId") UUID userId,
        @Param("category") String category
    );

    @Query("SELECT a FROM Anomaly a WHERE a.transactionId = :transactionId AND a.deletedAt IS NULL")
    List<Anomaly> findByTransactionId(@Param("transactionId") UUID transactionId);

    @Query("SELECT a FROM Anomaly a WHERE a.insightId = :insightId AND a.deletedAt IS NULL")
    List<Anomaly> findByInsightId(@Param("insightId") UUID insightId);

    @Query("SELECT a FROM Anomaly a WHERE a.userId = :userId AND a.merchantName = :merchantName AND a.deletedAt IS NULL ORDER BY a.detectedAt DESC")
    List<Anomaly> findByUserIdAndMerchant(
        @Param("userId") UUID userId,
        @Param("merchantName") String merchantName
    );

    @Query("SELECT COUNT(a) FROM Anomaly a WHERE a.userId = :userId AND a.isAcknowledged = false AND a.deletedAt IS NULL")
    long countUnacknowledgedByUserId(@Param("userId") UUID userId);

    @Query("SELECT a FROM Anomaly a WHERE a.userId = :userId AND a.detectedAt BETWEEN :start AND :end AND a.deletedAt IS NULL ORDER BY a.detectedAt DESC")
    List<Anomaly> findByUserIdAndDateRange(
        @Param("userId") UUID userId,
        @Param("start") Instant start,
        @Param("end") Instant end
    );

    @Query("SELECT a FROM Anomaly a WHERE a.userId = :userId AND a.isFalsePositive = true AND a.deletedAt IS NULL ORDER BY a.detectedAt DESC")
    List<Anomaly> findFalsePositivesByUserId(@Param("userId") UUID userId);

    @Query("SELECT a FROM Anomaly a WHERE a.deletedAt IS NULL ORDER BY a.detectedAt DESC")
    Page<Anomaly> findAllNotDeleted(Pageable pageable);
}
