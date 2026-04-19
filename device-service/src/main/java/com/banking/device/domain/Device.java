package com.banking.device.domain;

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
 * Device entity representing a registered user device with fingerprinting and trust scoring.
 * Stores device characteristics, trust metrics, and security status.
 */
@Entity
@Table(name = "device")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "fingerprint_hash", nullable = false, unique = true)
    private String fingerprintHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", nullable = false)
    private DeviceType deviceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_status", nullable = false)
    @Builder.Default
    private DeviceStatus deviceStatus = DeviceStatus.ACTIVE;

    @Column(name = "trust_score", nullable = false)
    @Builder.Default
    private Integer trustScore = 30;

    @Column(name = "last_seen_at", nullable = false)
    @Builder.Default
    private Instant lastSeenAt = Instant.now();

    // Device Information
    @Column(name = "browser", length = 100)
    private String browser;

    @Column(name = "browser_version", length = 50)
    private String browserVersion;

    @Column(name = "os", length = 100)
    private String os;

    @Column(name = "os_version", length = 50)
    private String osVersion;

    @Column(name = "device_name")
    private String deviceName;

    // Network Information
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "geolocation", columnDefinition = "jsonb")
    private String geolocation;

    // Hardware Information
    @Column(name = "screen_resolution", length = 50)
    private String screenResolution;

    @Column(name = "timezone", length = 100)
    private String timezone;

    @Column(name = "language", length = 50)
    private String language;

    @Column(name = "user_agent", columnDefinition = "text")
    private String userAgent;

    // Audit Fields
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
     * Checks if the device is soft deleted
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * Soft deletes the device
     */
    public void delete() {
        this.deletedAt = Instant.now();
    }

    /**
     * Updates the last seen timestamp
     */
    public void updateLastSeen() {
        this.lastSeenAt = Instant.now();
    }

    /**
     * Increases trust score by specified amount (max 100)
     */
    public void increaseTrust(int amount) {
        this.trustScore = Math.min(100, this.trustScore + amount);
    }

    /**
     * Decreases trust score by specified amount (min 0)
     */
    public void decreaseTrust(int amount) {
        this.trustScore = Math.max(0, this.trustScore - amount);
    }

    /**
     * Checks if device is trusted (trust score >= 70)
     */
    public boolean isTrusted() {
        return trustScore >= 70;
    }

    /**
     * Checks if device is suspicious (trust score <= 30)
     */
    public boolean isSuspicious() {
        return trustScore <= 30;
    }
}