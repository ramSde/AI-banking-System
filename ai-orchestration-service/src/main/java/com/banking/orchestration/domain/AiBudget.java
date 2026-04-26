package com.banking.orchestration.domain;

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
@Table(name = "ai_budgets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AiBudget {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "daily_budget_usd", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal dailyBudgetUsd = new BigDecimal("10.00");

    @Column(name = "monthly_budget_usd", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal monthlyBudgetUsd = new BigDecimal("300.00");

    @Column(name = "daily_spent_usd", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal dailySpentUsd = BigDecimal.ZERO;

    @Column(name = "monthly_spent_usd", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal monthlySpentUsd = BigDecimal.ZERO;

    @Column(name = "daily_reset_at", nullable = false)
    private Instant dailyResetAt;

    @Column(name = "monthly_reset_at", nullable = false)
    private Instant monthlyResetAt;

    @Column(name = "alert_threshold", nullable = false, precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal alertThreshold = new BigDecimal("0.80");

    @Column(name = "alert_sent", nullable = false)
    @Builder.Default
    private Boolean alertSent = false;

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
