package com.banking.identity.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

/**
 * RefreshTokenAudit Entity
 * 
 * Stores bcrypt hashes of refresh tokens for validation and rotation tracking.
 * Maintains audit trail of all refresh tokens issued, rotated, and revoked.
 */
@Entity
@Table(name = "refresh_token_audit")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "token_hash", nullable = false, unique = true, length = 255)
    private String tokenHash;

    @Column(name = "token_family_id", nullable = false)
    private UUID tokenFamilyId;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt = Instant.now();

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "replaced_by_token_id")
    private UUID replacedByTokenId;

    @Column(name = "device_id", length = 255)
    private String deviceId;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RefreshTokenStatus status = RefreshTokenStatus.ACTIVE;

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
    private Long version = 0L;

    /**
     * Check if token is expired
     */
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    /**
     * Check if token is active and not expired
     */
    public boolean isActive() {
        return status == RefreshTokenStatus.ACTIVE && !isExpired() && deletedAt == null;
    }

    /**
     * Revoke this token
     */
    public void revoke() {
        this.status = RefreshTokenStatus.REVOKED;
        this.revokedAt = Instant.now();
    }

    /**
     * Mark token as replaced by another token
     */
    public void markAsReplaced(UUID newTokenId) {
        this.status = RefreshTokenStatus.REPLACED;
        this.replacedByTokenId = newTokenId;
    }

    /**
     * Mark token as expired
     */
    public void markAsExpired() {
        this.status = RefreshTokenStatus.EXPIRED;
    }
}
