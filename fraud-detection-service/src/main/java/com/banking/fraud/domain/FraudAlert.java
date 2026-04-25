package com.banking.fraud.domain;

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

/**
 * Fraud Alert Entity
 * 
 * Tracks fraud alerts requiring investigation or action.
 */
@Entity
@Table(name = "fraud_alert")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FraudAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "fraud_check_id", nullable = false)
    private UUID fraudCheckId;

    @Column(name = "transaction_id", nullable = false)
    private UUID transactionId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "alert_type", nullable = false, length = 50)
    private String alertType;

    @Column(name = "severity", nullable = false, length = 20)
    private String severity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private AlertStatus status = AlertStatus.OPEN;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "assigned_to")
    private UUID assignedTo;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

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

    public boolean isOpen() {
        return status == AlertStatus.OPEN || status == AlertStatus.INVESTIGATING;
    }

    public boolean isResolved() {
        return status == AlertStatus.RESOLVED || status == AlertStatus.FALSE_POSITIVE;
    }
}
