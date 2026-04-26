package com.banking.chat.domain;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "message_feedback", indexes = {
    @Index(name = "idx_message_feedback_message_id", columnList = "message_id"),
    @Index(name = "idx_message_feedback_user_id", columnList = "user_id"),
    @Index(name = "idx_message_feedback_rating", columnList = "rating"),
    @Index(name = "idx_message_feedback_created_at", columnList = "created_at")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "message_id", nullable = false)
    private UUID messageId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "rating", nullable = false, length = 20)
    private FeedbackRating rating;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Type(JsonBinaryType.class)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;

    public enum FeedbackRating {
        POSITIVE,
        NEGATIVE,
        NEUTRAL
    }

    public boolean isPositive() {
        return rating == FeedbackRating.POSITIVE;
    }

    public boolean isNegative() {
        return rating == FeedbackRating.NEGATIVE;
    }
}
