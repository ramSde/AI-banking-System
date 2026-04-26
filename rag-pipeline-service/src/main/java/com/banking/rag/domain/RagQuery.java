package com.banking.rag.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "rag_queries")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class RagQuery {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "session_id")
    private UUID sessionId;

    @Column(name = "query_text", nullable = false, columnDefinition = "TEXT")
    private String queryText;

    @Column(name = "query_embedding", columnDefinition = "vector(1536)")
    private String queryEmbedding;

    @Column(name = "top_k", nullable = false)
    @Builder.Default
    private Integer topK = 10;

    @Column(name = "similarity_threshold", nullable = false, precision = 5, scale = 4)
    @Builder.Default
    private BigDecimal similarityThreshold = new BigDecimal("0.7000");

    @Column(name = "rerank_enabled", nullable = false)
    @Builder.Default
    private Boolean rerankEnabled = true;

    @Column(name = "max_context_tokens", nullable = false)
    @Builder.Default
    private Integer maxContextTokens = 4000;

    @Column(name = "retrieved_count", nullable = false)
    @Builder.Default
    private Integer retrievedCount = 0;

    @Column(name = "final_count", nullable = false)
    @Builder.Default
    private Integer finalCount = 0;

    @Column(name = "cache_hit", nullable = false)
    @Builder.Default
    private Boolean cacheHit = false;

    @Column(name = "retrieval_latency_ms")
    private Long retrievalLatencyMs;

    @Column(name = "rerank_latency_ms")
    private Long rerankLatencyMs;

    @Column(name = "total_latency_ms")
    private Long totalLatencyMs;

    @Column(name = "trace_id")
    private String traceId;

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
