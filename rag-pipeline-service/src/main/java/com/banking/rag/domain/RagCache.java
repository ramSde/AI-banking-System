package com.banking.rag.domain;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "rag_cache")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class RagCache {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "query_text", nullable = false, columnDefinition = "TEXT")
    private String queryText;

    @Column(name = "query_embedding", nullable = false, columnDefinition = "BYTEA")
    private byte[] queryEmbedding;

    @Type(JsonBinaryType.class)
    @Column(name = "cached_response", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> cachedResponse;

    @Column(name = "hit_count", nullable = false)
    @Builder.Default
    private Integer hitCount = 0;

    @Column(name = "last_hit_at")
    private Instant lastHitAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

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
}
