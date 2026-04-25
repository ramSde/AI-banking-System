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
import java.util.UUID;

/**
 * UserPreference entity representing user settings and notification preferences.
 * Stores user-specific configuration for UI, notifications, and security settings.
 */
@Entity
@Table(name = "user_preference")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class UserPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "language", nullable = false, length = 10)
    @Builder.Default
    private String language = "en";

    @Column(name = "timezone", nullable = false, length = 50)
    @Builder.Default
    private String timezone = "UTC";

    @Column(name = "currency", nullable = false, length = 3)
    @Builder.Default
    private String currency = "USD";

    @Column(name = "date_format", nullable = false, length = 20)
    @Builder.Default
    private String dateFormat = "YYYY-MM-DD";

    @Column(name = "time_format", nullable = false, length = 10)
    @Builder.Default
    private String timeFormat = "24H";

    @Column(name = "notification_email", nullable = false)
    @Builder.Default
    private Boolean notificationEmail = true;

    @Column(name = "notification_sms", nullable = false)
    @Builder.Default
    private Boolean notificationSms = true;

    @Column(name = "notification_push", nullable = false)
    @Builder.Default
    private Boolean notificationPush = true;

    @Column(name = "notification_transaction", nullable = false)
    @Builder.Default
    private Boolean notificationTransaction = true;

    @Column(name = "notification_login", nullable = false)
    @Builder.Default
    private Boolean notificationLogin = true;

    @Column(name = "notification_marketing", nullable = false)
    @Builder.Default
    private Boolean notificationMarketing = false;

    @Column(name = "two_factor_enabled", nullable = false)
    @Builder.Default
    private Boolean twoFactorEnabled = false;

    @Column(name = "biometric_enabled", nullable = false)
    @Builder.Default
    private Boolean biometricEnabled = false;

    @Column(name = "session_timeout_minutes", nullable = false)
    @Builder.Default
    private Integer sessionTimeoutMinutes = 30;

    @Column(name = "theme", nullable = false, length = 20)
    @Builder.Default
    private String theme = "LIGHT";

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
     * Checks if the preference is soft deleted
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * Soft deletes the preference
     */
    public void delete() {
        this.deletedAt = Instant.now();
    }

    /**
     * Checks if any notification is enabled
     */
    public boolean hasAnyNotificationEnabled() {
        return notificationEmail || notificationSms || notificationPush;
    }

    /**
     * Disables all notifications
     */
    public void disableAllNotifications() {
        this.notificationEmail = false;
        this.notificationSms = false;
        this.notificationPush = false;
        this.notificationTransaction = false;
        this.notificationLogin = false;
        this.notificationMarketing = false;
    }

    /**
     * Enables all notifications
     */
    public void enableAllNotifications() {
        this.notificationEmail = true;
        this.notificationSms = true;
        this.notificationPush = true;
        this.notificationTransaction = true;
        this.notificationLogin = true;
    }
}
