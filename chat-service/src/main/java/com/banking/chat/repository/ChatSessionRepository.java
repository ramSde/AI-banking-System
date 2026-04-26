package com.banking.chat.repository;

import com.banking.chat.domain.ChatSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, UUID> {

    @Query("SELECT s FROM ChatSession s WHERE s.id = :id AND s.deletedAt IS NULL")
    Optional<ChatSession> findByIdAndNotDeleted(@Param("id") UUID id);

    @Query("SELECT s FROM ChatSession s WHERE s.userId = :userId AND s.deletedAt IS NULL ORDER BY s.lastActivityAt DESC")
    Page<ChatSession> findByUserIdAndNotDeleted(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT s FROM ChatSession s WHERE s.userId = :userId AND s.status = :status AND s.deletedAt IS NULL ORDER BY s.lastActivityAt DESC")
    Page<ChatSession> findByUserIdAndStatusAndNotDeleted(
        @Param("userId") UUID userId,
        @Param("status") ChatSession.SessionStatus status,
        Pageable pageable
    );

    @Query("SELECT s FROM ChatSession s WHERE s.userId = :userId AND s.status = 'ACTIVE' AND s.deletedAt IS NULL ORDER BY s.lastActivityAt DESC")
    List<ChatSession> findActiveSessionsByUserId(@Param("userId") UUID userId);

    @Query("SELECT COUNT(s) FROM ChatSession s WHERE s.userId = :userId AND s.status = 'ACTIVE' AND s.deletedAt IS NULL")
    long countActiveSessionsByUserId(@Param("userId") UUID userId);

    @Query("SELECT s FROM ChatSession s WHERE s.status = 'ACTIVE' AND s.lastActivityAt < :threshold AND s.deletedAt IS NULL")
    List<ChatSession> findInactiveSessions(@Param("threshold") Instant threshold);

    @Modifying
    @Query("UPDATE ChatSession s SET s.status = 'INACTIVE', s.updatedAt = :now WHERE s.id IN :sessionIds")
    void markSessionsAsInactive(@Param("sessionIds") List<UUID> sessionIds, @Param("now") Instant now);

    @Modifying
    @Query("UPDATE ChatSession s SET s.deletedAt = :now, s.updatedAt = :now WHERE s.id = :id")
    void softDelete(@Param("id") UUID id, @Param("now") Instant now);

    @Query("SELECT s FROM ChatSession s WHERE s.userId = :userId AND s.title LIKE %:searchTerm% AND s.deletedAt IS NULL ORDER BY s.lastActivityAt DESC")
    Page<ChatSession> searchByUserIdAndTitle(
        @Param("userId") UUID userId,
        @Param("searchTerm") String searchTerm,
        Pageable pageable
    );
}
