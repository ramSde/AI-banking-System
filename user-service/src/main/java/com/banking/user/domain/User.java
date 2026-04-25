package com.banking.user.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * User entity representing a user profile with encrypted PII fields.
 * Stores user information with sensitive data encrypted at rest using AES-256-GCM.
 * 
 * Security:
 * - PII fields (phone, name, DOB, address) are encrypted
 * - Masked versions stored for display purposes
 * - Soft delete support for GDPR compliance
 */
@Entity
@Table(name = "\"user\"")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "phone_number_encrypted", nullable = false, columnDefinition = "TEXT")
    private String phoneNumberEncrypted;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "first_name_encrypted", nullable = false, columnDefinition = "TEXT")
    private String firstNameEncrypted;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "last_name_encrypted", nullable = false, columnDefinition = "TEXT")
    private String lastNameEncrypted;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "date_of_birth_encrypted", nullable = false, columnDefinition = "TEXT")
    private String dateOfBirthEncrypted;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "address_encrypted", columnDefinition = "TEXT")
    private String addressEncrypted;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "country", nullable = false, length = 100)
    @Builder.Default
    private String country = "IN";

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_status", nullable = false, length = 20)
    @Builder.Default
    private UserStatus userStatus = UserStatus.PENDING_VERIFICATION;

    @Enumerated(EnumType.STRING)
    @Column(name = "kyc_status", nullable = false, length = 20)
    @Builder.Default
    private KycStatus kycStatus = KycStatus.PENDING;

    @Column(name = "kyc_verified_at")
    private Instant kycVerifiedAt;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "failed_login_attempts", nullable = false)
    @Builder.Default
    private Integer failedLoginAttempts = 0;

    @Column(name = "account_locked_until")
    private Instant accountLockedUntil;

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

    /**
     * Checks if the user is soft deleted
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * Soft deletes the user
     */
    public void delete() {
        this.deletedAt = Instant.now();
    }

    /**
     * Checks if the user account is active
     */
    public boolean isActive() {
        return userStatus == UserStatus.ACTIVE && !isDeleted();
    }

    /**
     * Checks if the user account is locked
     */
    public boolean isLocked() {
        return userStatus == UserStatus.LOCKED || 
               (accountLockedUntil != null && accountLockedUntil.isAfter(Instant.now()));
    }

    /**
     * Checks if KYC is verified
     */
    public boolean isKycVerified() {
        return kycStatus == KycStatus.VERIFIED;
    }

    /**
     * Updates last login timestamp
     */
    public void updateLastLogin() {
        this.lastLoginAt = Instant.now();
        this.failedLoginAttempts = 0;
    }

    /**
     * Increments failed login attempts
     */
    public void incrementFailedLoginAttempts() {
        this.failedLoginAttempts++;
    }

    /**
     * Locks the account for specified duration
     */
    public void lockAccount(int durationMinutes) {
        this.userStatus = UserStatus.LOCKED;
        this.accountLockedUntil = Instant.now().plusSeconds(durationMinutes * 60L);
    }

    /**
     * Unlocks the account
     */
    public void unlockAccount() {
        this.userStatus = UserStatus.ACTIVE;
        this.accountLockedUntil = null;
        this.failedLoginAttempts = 0;
    }

    /**
     * Marks KYC as verified
     */
    public void verifyKyc() {
        this.kycStatus = KycStatus.VERIFIED;
        this.kycVerifiedAt = Instant.now();
    }

    /**
     * Rejects KYC
     */
    public void rejectKyc() {
        this.kycStatus = KycStatus.REJECTED;
        this.kycVerifiedAt = null;
    }
}
