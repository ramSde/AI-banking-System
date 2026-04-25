package com.banking.notification.repository;

import com.banking.notification.domain.NotificationChannel;
import com.banking.notification.domain.NotificationHistory;
import com.banking.notification.domain.NotificationStatus;
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
public interface NotificationHistoryRepository extends JpaRepository<NotificationHistory, UUID> {

    @Query("SELECT h FROM NotificationHistory h WHERE h.idempotencyKey = :idempotencyKey AND h.deletedAt IS NULL")
    Optional<NotificationHistory> findByIdempotencyKey(@Param("idempotencyKey") UUID idempotencyKey);

    @Query("SELECT h FROM NotificationHistory h WHERE h.userId = :userId AND h.deletedAt IS NULL ORDER BY h.createdAt DESC")
    Page<NotificationHistory> findByUserId(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT h FROM NotificationHistory h WHERE h.userId = :userId AND h.channel = :channel AND h.deletedAt IS NULL ORDER BY h.createdAt DESC")
    Page<NotificationHistory> findByUserIdAndChannel(
            @Param("userId") UUID userId,
            @Param("channel") NotificationChannel channel,
            Pageable pageable
    );

    @Query("SELECT h FROM NotificationHistory h WHERE h.userId = :userId AND h.status = :status AND h.deletedAt IS NULL ORDER BY h.createdAt DESC")
    Page<NotificationHistory> findByUserIdAndStatus(
            @Param("userId") UUID userId,
            @Param("status") NotificationStatus status,
            Pageable pageable
    );

    @Query("SELECT h FROM NotificationHistory h WHERE h.status = :status AND h.deletedAt IS NULL ORDER BY h.createdAt DESC")
    Page<NotificationHistory> findByStatus(@Param("status") NotificationStatus status, Pageable pageable);

    @Query("SELECT h FROM NotificationHistory h WHERE h.userId = :userId AND h.createdAt BETWEEN :startDate AND :endDate AND h.deletedAt IS NULL ORDER BY h.createdAt DESC")
    Page<NotificationHistory> findByUserIdAndDateRange(
            @Param("userId") UUID userId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            Pageable pageable
    );

    @Query("SELECT COUNT(h) FROM NotificationHistory h WHERE h.userId = :userId AND h.channel = :channel AND h.createdAt >= :since AND h.deletedAt IS NULL")
    Long countByUserIdAndChannelSince(
            @Param("userId") UUID userId,
            @Param("channel") NotificationChannel channel,
            @Param("since") Instant since
    );

    @Query("SELECT COUNT(h) FROM NotificationHistory h WHERE h.userId = :userId AND h.status = :status AND h.deletedAt IS NULL")
    Long countByUserIdAndStatus(
            @Param("userId") UUID userId,
            @Param("status") NotificationStatus status
    );
}
