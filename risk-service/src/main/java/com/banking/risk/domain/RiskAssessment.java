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
 * Risk Assessment entity representing a risk evaluation for an authentication attempt.
 * Stores the calculated risk score, level, and recommended action.
 */
@Entity
@Table(name = "risk_assessment")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    @Column(name = "risk_score", nullable = false)
    private Integer riskScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false, length = 20)
    private RiskLevel riskLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_action", nullable = false, length = 20)
    private RiskAction riskAction;

    @Column(name = "device_fingerprint", length = 255)
    private String deviceFingerprint;

    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "geolocation", columnDefinition = "jsonb")
    private Map<String, Object> geolocation;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "factors", nullable = false, columnDefinition = "jsonb")
    private Map<String, Integer> factors;

    @Column(name = "assessed_at", nullable = false)
    private Instant assessedAt;

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
     * Soft delete this risk assessment.
     */
    public void softDelete() {
        this.deletedAt = Instant.now();
    }

    /**
     * Check if this risk assessment is deleted.
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}
