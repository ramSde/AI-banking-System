package com.banking.fraud.domain;

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
 * Fraud Check Entity
 * 
 * Stores fraud check results for each transaction.
 */
@Entity
@Table(name = "fraud_check")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FraudCheck {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "transaction_id", nullable = false)
    private UUID transactionId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "risk_score", nullable = false)
    private Integer riskScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false, length = 20)
    private RiskLevel riskLevel;

    @Column(name = "blocked", nullable = false)
    @Builder.Default
    private Boolean blocked = false;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "rules_triggered", columnDefinition = "jsonb")
    private Map<String, Object> rulesTriggered;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "check_details", columnDefinition = "jsonb")
    private Map<String, Object> checkDetails;

    @Column(name = "checked_at", nullable = false)
    @Builder.Default
    private Instant checkedAt = Instant.now();

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
