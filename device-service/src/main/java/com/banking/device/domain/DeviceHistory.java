package com.banking.device.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

/**
 * Device history entity for immutable audit trail of device events.
 * Records all significant device-related activities and changes.
 */
@Entity
@Table(name = "device_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class DeviceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "device_id", nullable = false)
    private UUID deviceId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventType eventType;

    // Event Details
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "geolocation", columnDefinition = "jsonb")
    private String geolocation;

    @Column(name = "user_agent", columnDefinition = "text")
    private String userAgent;

    // Result
    @Column(name = "success", nullable = false)
    @Builder.Default
    private Boolean success = true;

    @Column(name = "failure_reason")
    private String failureReason;

    // Metadata
    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata;

    // Audit Fields
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Creates a successful device history entry
     */
    public static DeviceHistory success(UUID deviceId, UUID userId, EventType eventType) {
        return DeviceHistory.builder()
                .deviceId(deviceId)
                .userId(userId)
                .eventType(eventType)
                .success(true)
                .build();
    }

    /**
     * Creates a failed device history entry
     */
    public static DeviceHistory failure(UUID deviceId, UUID userId, EventType eventType, String reason) {
        return DeviceHistory.builder()
                .deviceId(deviceId)
                .userId(userId)
                .eventType(eventType)
                .success(false)
                .failureReason(reason)
                .build();
    }

    /**
     * Adds context information to the history entry
     */
    public DeviceHistory withContext(String ipAddress, String geolocation, String userAgent) {
        this.ipAddress = ipAddress;
        this.geolocation = geolocation;
        this.userAgent = userAgent;
        return this;
    }

    /**
     * Adds metadata to the history entry
     */
    public DeviceHistory withMetadata(String metadata) {
        this.metadata = metadata;
        return this;
    }
}