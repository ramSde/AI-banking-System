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
 * Fraud Pattern Entity
 * 
 * Tracks detected fraud patterns for behavioral analysis.
 */
@Entity
@Table(name = "fraud_pattern")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FraudPattern {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "pattern_type", nullable = false, length = 50)
    private String patternType;

    @Column(name = "pattern_name", nullable = false, length = 100)
    private String patternName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "detection_count", nullable = false)
    @Builder.Default
    private Integer detectionCount = 1;

    @Column(name = "first_detected_at", nullable = false)
    @Builder.Default
    private Instant firstDetectedAt = Instant.now();

    @Column(name = "last_detected_at", nullable = false)
    @Builder.Default
    private Instant lastDetectedAt = Instant.now();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "pattern_data", columnDefinition = "jsonb")
    private Map<String, Object> patternData;

    @Column(name = "severity", nullable = false, length = 20)
    private String severity;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

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

    public void incrementDetectionCount() {
        this.detectionCount++;
        this.lastDetectedAt = Instant.now();
    }
}
