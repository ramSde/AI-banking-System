package com.banking.transaction.domain;

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

/**
 * Transaction Hold Entity
 * 
 * Represents an authorization hold on an account balance.
 * Used for pre-authorization flows (e.g., hotel reservations, car rentals).
 */
@Entity
@Table(name = "transaction_hold")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionHold {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "hold_reference", nullable = false, unique = true, length = 50)
    private String holdReference;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    @Builder.Default
    private String currency = "USD";

    @Enumerated(EnumType.STRING)
    @Column(name = "hold_type", nullable = false, length = 20)
    private HoldType holdType;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "initiated_by", nullable = false)
    private UUID initiatedBy;

    @Column(name = "initiated_at", nullable = false)
    @Builder.Default
    private Instant initiatedAt = Instant.now();

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "released_at")
    private Instant releasedAt;

    @Column(name = "captured_transaction_id")
    private UUID capturedTransactionId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;

    /**
     * Check if hold is active
     */
    public boolean isActive() {
        return releasedAt == null && capturedTransactionId == null && !isExpired();
    }

    /**
     * Check if hold is expired
     */
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    /**
     * Check if hold is released
     */
    public boolean isReleased() {
        return releasedAt != null;
    }

    /**
     * Check if hold is captured
     */
    public boolean isCaptured() {
        return capturedTransactionId != null;
    }
}
