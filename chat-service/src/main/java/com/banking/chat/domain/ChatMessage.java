package com.banking.chat.domain;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "chat_messages", indexes = {
    @Index(name = "idx_chat_messages_session_id", columnList = "session_id"),
    @Index(name = "idx_chat_messages_role", columnList = "role"),
    @Index(name = "idx_chat_messages_created_at", columnList = "created_at"),
    @Index(name = "idx_chat_messages_session_created", columnList = "session_id, created_at")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private MessageRole role;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "model_name", length = 100)
    private String modelName;

    @Column(name = "input_tokens")
    private Integer inputTokens;

    @Column(name = "output_tokens")
    private Integer outputTokens;

    @Column(name = "total_tokens")
    private Integer totalTokens;

    @Column(name = "latency_ms")
    private Long latencyMs;

    @Type(JsonBinaryType.class)
    @Column(name = "sources", columnDefinition = "jsonb")
    private List<Map<String, Object>> sources;

    @Type(JsonBinaryType.class)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @Column(name = "parent_message_id")
    private UUID parentMessageId;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "is_error", nullable = false)
    @Builder.Default
    private Boolean isError = false;

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

    public enum MessageRole {
        USER,
        ASSISTANT,
        SYSTEM
    }

    @PrePersist
    public void prePersist() {
        if (isError == null) {
            isError = false;
        }
    }

    public boolean isUserMessage() {
        return role == MessageRole.USER;
    }

    public boolean isAssistantMessage() {
        return role == MessageRole.ASSISTANT;
    }

    public boolean hasError() {
        return Boolean.TRUE.equals(isError);
    }
}
