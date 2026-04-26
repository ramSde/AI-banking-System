package com.banking.insight.domain;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "recommendations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "insight_id")
    private UUID insightId;

    @Enumerated(EnumType.STRING)
    @Column(name = "recommendation_type", nullable = false, length = 50)
    private RecommendationType recommendationType;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Type(JsonBinaryType.class)
    @Column(name = "action_items", nullable = false, columnDefinition = "jsonb")
    private List<String> actionItems;

    @Column(name = "potential_savings", precision = 15, scale = 2)
    private BigDecimal potentialSavings;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    @Builder.Default
    private Priority priority = Priority.MEDIUM;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "confidence_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal confidenceScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private Status status = Status.PENDING;

    @Column(name = "is_accepted", nullable = false)
    @Builder.Default
    private Boolean isAccepted = false;

    @Column(name = "is_dismissed", nullable = false)
    @Builder.Default
    private Boolean isDismissed = false;

    @Column(name = "accepted_at")
    private Instant acceptedAt;

    @Column(name = "dismissed_at")
    private Instant dismissedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

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

    public enum RecommendationType {
        SAVE_MONEY,
        REDUCE_SPENDING,
        OPTIMIZE_BUDGET,
        INVESTMENT,
        DEBT_REDUCTION,
        SUBSCRIPTION_REVIEW
    }

    public enum Priority {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    public enum Status {
        PENDING,
        ACCEPTED,
        DISMISSED,
        EXPIRED,
        COMPLETED
    }

    public void accept() {
        this.isAccepted = true;
        this.status = Status.ACCEPTED;
        this.acceptedAt = Instant.now();
    }

    public void dismiss() {
        this.isDismissed = true;
        this.status = Status.DISMISSED;
        this.dismissedAt = Instant.now();
    }

    public void complete() {
        this.status = Status.COMPLETED;
    }

    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(Instant.now());
    }

    public boolean isActionable() {
        return status == Status.PENDING && !isExpired();
    }
}
