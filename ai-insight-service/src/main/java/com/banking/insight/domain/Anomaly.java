package com.banking.insight.domain;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "anomalies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Anomaly {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "insight_id")
    private UUID insightId;

    @Column(name = "transaction_id")
    private UUID transactionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "anomaly_type", nullable = false, length = 50)
    private AnomalyType anomalyType;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 20)
    private Severity severity;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "detected_value", nullable = false, precision = 15, scale = 2)
    private BigDecimal detectedValue;

    @Column(name = "expected_value", precision = 15, scale = 2)
    private BigDecimal expectedValue;

    @Column(name = "deviation_percentage", precision = 5, scale = 2)
    private BigDecimal deviationPercentage;

    @Column(name = "z_score", precision = 10, scale = 4)
    private BigDecimal zScore;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "merchant_name")
    private String merchantName;

    @Enumerated(EnumType.STRING)
    @Column(name = "detection_method", nullable = false, length = 50)
    private DetectionMethod detectionMethod;

    @Column(name = "confidence_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal confidenceScore;

    @Column(name = "is_false_positive", nullable = false)
    @Builder.Default
    private Boolean isFalsePositive = false;

    @Column(name = "is_acknowledged", nullable = false)
    @Builder.Default
    private Boolean isAcknowledged = false;

    @Column(name = "acknowledged_at")
    private Instant acknowledgedAt;

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    @Type(JsonBinaryType.class)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @Column(name = "detected_at", nullable = false)
    private Instant detectedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Integer version = 0;

    @PrePersist
    protected void onCreate() {
        final Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.detectedAt == null) {
            this.detectedAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public enum AnomalyType {
        UNUSUAL_AMOUNT,
        UNUSUAL_MERCHANT,
        UNUSUAL_CATEGORY,
        UNUSUAL_FREQUENCY,
        UNUSUAL_TIME
    }

    public enum Severity {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    public enum DetectionMethod {
        Z_SCORE,
        IQR,
        ISOLATION_FOREST,
        STATISTICAL
    }

    public void acknowledge(String notes) {
        this.isAcknowledged = true;
        this.acknowledgedAt = Instant.now();
        this.resolutionNotes = notes;
    }

    public void markAsFalsePositive(String notes) {
        this.isFalsePositive = true;
        this.isAcknowledged = true;
        this.acknowledgedAt = Instant.now();
        this.resolutionNotes = notes;
    }
}
