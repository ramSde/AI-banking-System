package com.banking.insight.repository;

import com.banking.insight.domain.Insight;
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
public interface InsightRepository extends JpaRepository<Insight, UUID> {

    @Query("SELECT i FROM Insight i WHERE i.id = :id AND i.deletedAt IS NULL")
    Optional<Insight> findByIdAndNotDeleted(@Param("id") UUID id);

    @Query("SELECT i FROM Insight i WHERE i.userId = :userId AND i.deletedAt IS NULL ORDER BY i.createdAt DESC")
    Page<Insight> findByUserIdAndNotDeleted(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT i FROM Insight i WHERE i.userId = :userId AND i.insightType = :type AND i.deletedAt IS NULL ORDER BY i.createdAt DESC")
    Page<Insight> findByUserIdAndTypeAndNotDeleted(
        @Param("userId") UUID userId,
        @Param("type") Insight.InsightType type,
        Pageable pageable
    );

    @Query("SELECT i FROM Insight i WHERE i.userId = :userId AND i.isRead = false AND i.deletedAt IS NULL ORDER BY i.priority DESC, i.createdAt DESC")
    List<Insight> findUnreadByUserId(@Param("userId") UUID userId);

    @Query("SELECT i FROM Insight i WHERE i.userId = :userId AND i.isDismissed = false AND i.validFrom <= :now AND (i.validUntil IS NULL OR i.validUntil > :now) AND i.deletedAt IS NULL ORDER BY i.priority DESC, i.createdAt DESC")
    List<Insight> findActiveByUserId(@Param("userId") UUID userId, @Param("now") Instant now);

    @Query("SELECT i FROM Insight i WHERE i.userId = :userId AND i.priority = :priority AND i.deletedAt IS NULL ORDER BY i.createdAt DESC")
    List<Insight> findByUserIdAndPriority(
        @Param("userId") UUID userId,
        @Param("priority") Insight.Priority priority
    );

    @Query("SELECT i FROM Insight i WHERE i.userId = :userId AND i.category = :category AND i.deletedAt IS NULL ORDER BY i.createdAt DESC")
    List<Insight> findByUserIdAndCategory(
        @Param("userId") UUID userId,
        @Param("category") String category
    );

    @Query("SELECT COUNT(i) FROM Insight i WHERE i.userId = :userId AND i.isRead = false AND i.deletedAt IS NULL")
    long countUnreadByUserId(@Param("userId") UUID userId);

    @Query("SELECT i FROM Insight i WHERE i.validUntil < :now AND i.isDismissed = false AND i.deletedAt IS NULL")
    List<Insight> findExpiredInsights(@Param("now") Instant now);

    @Query("SELECT i FROM Insight i WHERE i.deletedAt IS NULL ORDER BY i.createdAt DESC")
    Page<Insight> findAllNotDeleted(Pageable pageable);
}
