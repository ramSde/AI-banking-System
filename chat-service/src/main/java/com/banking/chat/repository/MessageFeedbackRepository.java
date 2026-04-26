package com.banking.chat.repository;

import com.banking.chat.domain.MessageFeedback;
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
public interface MessageFeedbackRepository extends JpaRepository<MessageFeedback, UUID> {

    @Query("SELECT f FROM MessageFeedback f WHERE f.id = :id AND f.deletedAt IS NULL")
    Optional<MessageFeedback> findByIdAndNotDeleted(@Param("id") UUID id);

    @Query("SELECT f FROM MessageFeedback f WHERE f.messageId = :messageId AND f.deletedAt IS NULL")
    List<MessageFeedback> findByMessageIdAndNotDeleted(@Param("messageId") UUID messageId);

    @Query("SELECT f FROM MessageFeedback f WHERE f.messageId = :messageId AND f.userId = :userId AND f.deletedAt IS NULL")
    Optional<MessageFeedback> findByMessageIdAndUserIdAndNotDeleted(
        @Param("messageId") UUID messageId,
        @Param("userId") UUID userId
    );

    @Query("SELECT f FROM MessageFeedback f WHERE f.userId = :userId AND f.deletedAt IS NULL ORDER BY f.createdAt DESC")
    Page<MessageFeedback> findByUserIdAndNotDeleted(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT f FROM MessageFeedback f WHERE f.rating = :rating AND f.deletedAt IS NULL ORDER BY f.createdAt DESC")
    Page<MessageFeedback> findByRatingAndNotDeleted(
        @Param("rating") MessageFeedback.FeedbackRating rating,
        Pageable pageable
    );

    @Query("SELECT COUNT(f) FROM MessageFeedback f WHERE f.messageId = :messageId AND f.rating = :rating AND f.deletedAt IS NULL")
    long countByMessageIdAndRating(
        @Param("messageId") UUID messageId,
        @Param("rating") MessageFeedback.FeedbackRating rating
    );

    @Query("SELECT COUNT(f) FROM MessageFeedback f WHERE f.messageId = :messageId AND f.deletedAt IS NULL")
    long countByMessageId(@Param("messageId") UUID messageId);

    @Modifying
    @Query("UPDATE MessageFeedback f SET f.deletedAt = :now, f.updatedAt = :now WHERE f.id = :id")
    void softDelete(@Param("id") UUID id, @Param("now") Instant now);

    @Query("SELECT f.rating, COUNT(f) FROM MessageFeedback f WHERE f.deletedAt IS NULL GROUP BY f.rating")
    List<Object[]> getFeedbackStatistics();
}
