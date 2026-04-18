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
 * User Entity
 * 
 * Represents core user identity and authentication status.
 * Stores user profile information, verification status, and security metadata.
 */
@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "username", unique = true, length = 100)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = false;

    @Column(name = "phone_verified", nullable = false)
    private Boolean phoneVerified = false;

    @Column(name = "failed_login_attempts", nullable = false)
    private Integer failedLoginAttempts = 0;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "last_login_ip", length = 45)
    private String lastLoginIp;

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
     * Check if user account is currently locked
     */
    public boolean isLocked() {
        return lockedUntil != null && Instant.now().isBefore(lockedUntil);
    }

    /**
     * Check if user account is active and not locked
     */
    public boolean isActive() {
        return status == UserStatus.ACTIVE && !isLocked() && deletedAt == null;
    }

    /**
     * Increment failed login attempts
     */
    public void incrementFailedLoginAttempts() {
        this.failedLoginAttempts++;
    }

    /**
     * Reset failed login attempts (after successful login)
     */
    public void resetFailedLoginAttempts() {
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
    }

    /**
     * Lock account until specified time
     */
    public void lockUntil(Instant until) {
        this.lockedUntil = until;
    }

    /**
     * Record successful login
     */
    public void recordSuccessfulLogin(String ipAddress) {
        this.lastLoginAt = Instant.now();
        this.lastLoginIp = ipAddress;
        resetFailedLoginAttempts();
    }
}
