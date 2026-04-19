package com.banking.device.domain;

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
 * Device anomaly entity for tracking suspicious device behavior and security incidents.
 * Stores detected anomalies with risk assessment and resolution tracking.
 */
@Entity
@Table(name = "device_anomaly")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class DeviceAnomaly {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "device_id", nullable = false)
    private UUID deviceId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "anomaly_type", nullable = false)
    private AnomalyType anomalyType;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false)
    @Builder.Default
    private Severity severity = Severity.MEDIUM;

    // Anomaly Details
    @Column(name = "description", nullable = false, columnDefinition = "text")
    private String description;

    @Column(name = "detected_at", nullable = false)
    @Builder.Default
    private Instant detectedAt = Instant.now();

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "resolution_notes", columnDefinition = "text")
    private String resolutionNotes;

    // Context Information
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "geolocation", columnDefinition = "jsonb")
    private String geolocation;

    @Column(name = "user_agent", columnDefinition = "text")
    private String userAgent;

    // Risk Assessment
    @Column(name = "risk_score", nullable = false)
    @Builder.Default
    private Integer riskScore = 50;

    @Column(name = "confidence_level", nullable = false, precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal confidenceLevel = BigDecimal.valueOf(0.75);

    // Metadata
    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata;

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
     * Checks if the anomaly is resolved
     */
    public boolean isResolved() {
        return resolvedAt != null;
    }

    /**
     * Checks if the anomaly is soft deleted
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * Resolves the anomaly with notes
     */
    public void resolve(String notes) {
        this.resolvedAt = Instant.now();
        this.resolutionNotes = notes;
    }

    /**
     * Soft deletes the anomaly
     */
    public void delete() {
        this.deletedAt = Instant.now();
    }

    /**
     * Checks if anomaly is high risk (score >= 70)
     */
    public boolean isHighRisk() {
        return riskScore >= 70;
    }

    /**
     * Checks if anomaly is critical severity
     */
    public boolean isCritical() {
        return severity == Severity.CRITICAL;
    }

    /**
     * Checks if confidence level is high (>= 0.8)
     */
    public boolean isHighConfidence() {
        return confidenceLevel.compareTo(BigDecimal.valueOf(0.8)) >= 0;
    }

    /**
     * Creates a new device anomaly with context
     */
    public static DeviceAnomaly create(UUID deviceId, UUID userId, AnomalyType type, 
                                     String description, Severity severity) {
        return DeviceAnomaly.builder()
                .deviceId(deviceId)
                .userId(userId)
                .anomalyType(type)
                .description(description)
                .severity(severity)
                .build();
    }

    /**
     * Adds context information to the anomaly
     */
    public DeviceAnomaly withContext(String ipAddress, String geolocation, String userAgent) {
        this.ipAddress = ipAddress;
        this.geolocation = geolocation;
        this.userAgent = userAgent;
        return this;
    }

    /**
     * Sets risk assessment values
     */
    public DeviceAnomaly withRiskAssessment(Integer riskScore, BigDecimal confidenceLevel) {
        this.riskScore = riskScore;
        this.confidenceLevel = confidenceLevel;
        return this;
    }
}