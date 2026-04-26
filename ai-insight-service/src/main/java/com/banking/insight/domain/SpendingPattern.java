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
@Table(name = "spending_patterns")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpendingPattern {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "pattern_type", nullable = false, length = 50)
    private PatternType patternType;

    @Column(name = "category", nullable = false, length = 100)
    private String category;

    @Column(name = "merchant_name")
    private String merchantName;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequency", nullable = false, length = 20)
    private Frequency frequency;

    @Column(name = "average_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal averageAmount;

    @Column(name = "min_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal minAmount;

    @Column(name = "max_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal maxAmount;

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "transaction_count", nullable = false)
    private Integer transactionCount;

    @Column(name = "first_occurrence", nullable = false)
    private Instant firstOccurrence;

    @Column(name = "last_occurrence", nullable = false)
    private Instant lastOccurrence;

    @Column(name = "next_predicted_date")
    private Instant nextPredictedDate;

    @Column(name = "confidence_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal confidenceScore;

    @Column(name = "is_recurring", nullable = false)
    @Builder.Default
    private Boolean isRecurring = false;

    @Column(name = "is_seasonal", nullable = false)
    @Builder.Default
    private Boolean isSeasonal = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "season", length = 20)
    private Season season;

    @Enumerated(EnumType.STRING)
    @Column(name = "trend", length = 20)
    private Trend trend;

    @Type(JsonBinaryType.class)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

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
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public enum PatternType {
        RECURRING,
        SEASONAL,
        TRENDING_UP,
        TRENDING_DOWN,
        STABLE
    }

    public enum Frequency {
        DAILY,
        WEEKLY,
        BIWEEKLY,
        MONTHLY,
        QUARTERLY,
        YEARLY
    }

    public enum Season {
        SPRING,
        SUMMER,
        FALL,
        WINTER
    }

    public enum Trend {
        INCREASING,
        DECREASING,
        STABLE
    }
}
