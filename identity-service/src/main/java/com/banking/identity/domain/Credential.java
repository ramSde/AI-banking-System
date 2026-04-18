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
 * Credential Entity
 * 
 * Stores user authentication credentials (bcrypt hashed passwords).
 * One-to-one relationship with User entity.
 */
@Entity
@Table(name = "credentials")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Credential {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "password_changed_at", nullable = false)
    private Instant passwordChangedAt = Instant.now();

    @Column(name = "must_change_password", nullable = false)
    private Boolean mustChangePassword = false;

    @Column(name = "previous_password_hashes", columnDefinition = "TEXT[]")
    private String[] previousPasswordHashes;

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
     * Update password hash and record change timestamp
     */
    public void updatePasswordHash(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
        this.passwordChangedAt = Instant.now();
        this.mustChangePassword = false;
    }

    /**
     * Check if password was recently changed (within specified days)
     */
    public boolean wasPasswordChangedWithinDays(int days) {
        Instant threshold = Instant.now().minusSeconds(days * 24L * 60L * 60L);
        return passwordChangedAt.isAfter(threshold);
    }
}
