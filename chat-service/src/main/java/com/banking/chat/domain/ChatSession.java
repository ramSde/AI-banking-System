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
@Table(name = "chat_sessions", indexes = {
    @Index(name = "idx_chat_sessions_user_id", columnList = "user_id"),
    @Index(name = "idx_chat_sessions_status", columnList = "status"),
    @Index(name = "idx_chat_sessions_created_at", columnList = "created_at"),
    @Index(name = "idx_chat_sessions_user_status", columnList = "user_id, status")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "title", length = 500)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SessionStatus status;

    @Column(name = "message_count", nullable = false)
    @Builder.Default
    private Integer messageCount = 0;

    @Column(name = "total_tokens", nullable = false)
    @Builder.Default
    private Integer totalTokens = 0;

    @Type(JsonBinaryType.class)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @Column(name = "last_activity_at")
    private Instant lastActivityAt;

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

    public enum SessionStatus {
        ACTIVE,
        INACTIVE,
        ARCHIVED,
        DELETED
    }

    @PrePersist
    public void prePersist() {
        if (status == null) {
            status = SessionStatus.ACTIVE;
        }
        if (lastActivityAt == null) {
            lastActivityAt = Instant.now();
        }
    }

    public void incrementMessageCount() {
        this.messageCount++;
        this.lastActivityAt = Instant.now();
    }

    public void addTokens(int tokens) {
        this.totalTokens += tokens;
    }

    public boolean isActive() {
        return status == SessionStatus.ACTIVE && deletedAt == null;
    }
}
