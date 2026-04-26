package com.banking.chat.repository;

import com.banking.chat.domain.ChatMessage;
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
public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    @Query("SELECT m FROM ChatMessage m WHERE m.id = :id AND m.deletedAt IS NULL")
    Optional<ChatMessage> findByIdAndNotDeleted(@Param("id") UUID id);

    @Query("SELECT m FROM ChatMessage m WHERE m.sessionId = :sessionId AND m.deletedAt IS NULL ORDER BY m.createdAt ASC")
    List<ChatMessage> findBySessionIdAndNotDeleted(@Param("sessionId") UUID sessionId);

    @Query("SELECT m FROM ChatMessage m WHERE m.sessionId = :sessionId AND m.deletedAt IS NULL ORDER BY m.createdAt DESC")
    Page<ChatMessage> findBySessionIdAndNotDeletedPageable(@Param("sessionId") UUID sessionId, Pageable pageable);

    @Query("SELECT m FROM ChatMessage m WHERE m.sessionId = :sessionId AND m.deletedAt IS NULL ORDER BY m.createdAt DESC")
    List<ChatMessage> findRecentMessagesBySessionId(@Param("sessionId") UUID sessionId, Pageable pageable);

    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.sessionId = :sessionId AND m.deletedAt IS NULL")
    long countBySessionIdAndNotDeleted(@Param("sessionId") UUID sessionId);

    @Query("SELECT m FROM ChatMessage m WHERE m.sessionId = :sessionId AND m.role = :role AND m.deletedAt IS NULL ORDER BY m.createdAt ASC")
    List<ChatMessage> findBySessionIdAndRole(
        @Param("sessionId") UUID sessionId,
        @Param("role") ChatMessage.MessageRole role
    );

    @Query("SELECT SUM(m.totalTokens) FROM ChatMessage m WHERE m.sessionId = :sessionId AND m.deletedAt IS NULL")
    Long sumTokensBySessionId(@Param("sessionId") UUID sessionId);

    @Query("SELECT m FROM ChatMessage m WHERE m.sessionId = :sessionId AND m.isError = true AND m.deletedAt IS NULL ORDER BY m.createdAt DESC")
    List<ChatMessage> findErrorMessagesBySessionId(@Param("sessionId") UUID sessionId);

    @Modifying
    @Query("UPDATE ChatMessage m SET m.deletedAt = :now, m.updatedAt = :now WHERE m.id = :id")
    void softDelete(@Param("id") UUID id, @Param("now") Instant now);

    @Modifying
    @Query("UPDATE ChatMessage m SET m.deletedAt = :now, m.updatedAt = :now WHERE m.sessionId = :sessionId")
    void softDeleteBySessionId(@Param("sessionId") UUID sessionId, @Param("now") Instant now);

    @Query("SELECT m FROM ChatMessage m WHERE m.parentMessageId = :parentId AND m.deletedAt IS NULL ORDER BY m.createdAt ASC")
    List<ChatMessage> findByParentMessageId(@Param("parentId") UUID parentId);
}
