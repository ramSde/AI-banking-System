package com.banking.insight.repository;

import com.banking.insight.domain.Recommendation;
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
public interface RecommendationRepository extends JpaRepository<Recommendation, UUID> {

    @Query("SELECT r FROM Recommendation r WHERE r.id = :id AND r.deletedAt IS NULL")
    Optional<Recommendation> findByIdAndNotDeleted(@Param("id") UUID id);

    @Query("SELECT r FROM Recommendation r WHERE r.userId = :userId AND r.deletedAt IS NULL ORDER BY r.priority DESC, r.createdAt DESC")
    Page<Recommendation> findByUserIdAndNotDeleted(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT r FROM Recommendation r WHERE r.userId = :userId AND r.status = :status AND r.deletedAt IS NULL ORDER BY r.priority DESC, r.createdAt DESC")
    List<Recommendation> findByUserIdAndStatus(
        @Param("userId") UUID userId,
        @Param("status") Recommendation.Status status
    );

    @Query("SELECT r FROM Recommendation r WHERE r.userId = :userId AND r.status = 'PENDING' AND (r.expiresAt IS NULL OR r.expiresAt > :now) AND r.deletedAt IS NULL ORDER BY r.priority DESC, r.createdAt DESC")
    List<Recommendation> findActionableByUserId(@Param("userId") UUID userId, @Param("now") Instant now);

    @Query("SELECT r FROM Recommendation r WHERE r.userId = :userId AND r.recommendationType = :type AND r.deletedAt IS NULL ORDER BY r.createdAt DESC")
    List<Recommendation> findByUserIdAndType(
        @Param("userId") UUID userId,
        @Param("type") Recommendation.RecommendationType type
    );

    @Query("SELECT r FROM Recommendation r WHERE r.userId = :userId AND r.priority = :priority AND r.deletedAt IS NULL ORDER BY r.createdAt DESC")
    List<Recommendation> findByUserIdAndPriority(
        @Param("userId") UUID userId,
        @Param("priority") Recommendation.Priority priority
    );

    @Query("SELECT r FROM Recommendation r WHERE r.userId = :userId AND r.category = :category AND r.deletedAt IS NULL ORDER BY r.createdAt DESC")
    List<Recommendation> findByUserIdAndCategory(
        @Param("userId") UUID userId,
        @Param("category") String category
    );

    @Query("SELECT r FROM Recommendation r WHERE r.insightId = :insightId AND r.deletedAt IS NULL")
    List<Recommendation> findByInsightId(@Param("insightId") UUID insightId);

    @Query("SELECT COUNT(r) FROM Recommendation r WHERE r.userId = :userId AND r.status = 'PENDING' AND r.deletedAt IS NULL")
    long countPendingByUserId(@Param("userId") UUID userId);

    @Query("SELECT r FROM Recommendation r WHERE r.expiresAt < :now AND r.status = 'PENDING' AND r.deletedAt IS NULL")
    List<Recommendation> findExpiredRecommendations(@Param("now") Instant now);

    @Query("SELECT SUM(r.potentialSavings) FROM Recommendation r WHERE r.userId = :userId AND r.status = 'ACCEPTED' AND r.deletedAt IS NULL")
    Optional<java.math.BigDecimal> calculateTotalPotentialSavings(@Param("userId") UUID userId);

    @Query("SELECT r FROM Recommendation r WHERE r.deletedAt IS NULL ORDER BY r.createdAt DESC")
    Page<Recommendation> findAllNotDeleted(Pageable pageable);
}
