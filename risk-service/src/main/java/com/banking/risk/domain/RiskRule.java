package com.banking.risk.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Risk Rule entity representing a configurable risk assessment rule.
 * Rules define conditions and their impact on the overall risk score.
 */
@Entity
@Table(name = "risk_rule")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskRule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_type", nullable = false, length = 50)
    private RiskRuleType ruleType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "condition", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> condition;

    @Column(name = "risk_score_impact", nullable = false)
    private Integer riskScoreImpact;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled;

    @Column(name = "priority", nullable = false)
    private Integer priority;

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
    private Long version;

    /**
     * Soft delete this risk rule.
     */
    public void softDelete() {
        this.deletedAt = Instant.now();
    }

    /**
     * Check if this risk rule is deleted.
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    /**
     * Check if this risk rule is active (enabled and not deleted).
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(this.enabled) && this.deletedAt == null;
    }
}
