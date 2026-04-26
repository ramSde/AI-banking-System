package com.banking.orchestration.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ai_quotas")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AiQuota {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "user_tier", nullable = false, length = 50)
    @Builder.Default
    private String userTier = "FREE";

    @Column(name = "daily_token_limit", nullable = false)
    @Builder.Default
    private Integer dailyTokenLimit = 10000;

    @Column(name = "monthly_token_limit", nullable = false)
    @Builder.Default
    private Integer monthlyTokenLimit = 300000;

    @Column(name = "daily_tokens_used", nullable = false)
    @Builder.Default
    private Integer dailyTokensUsed = 0;

    @Column(name = "monthly_tokens_used", nullable = false)
    @Builder.Default
    private Integer monthlyTokensUsed = 0;

    @Column(name = "daily_reset_at", nullable = false)
    private Instant dailyResetAt;

    @Column(name = "monthly_reset_at", nullable = false)
    private Instant monthlyResetAt;

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
