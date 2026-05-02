package com.banking.i18n.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "translations", 
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_translations_key_locale", columnNames = {"translation_key_id", "locale_code"})
    },
    indexes = {
        @Index(name = "idx_translations_key_id", columnList = "translation_key_id"),
        @Index(name = "idx_translations_locale", columnList = "locale_code"),
        @Index(name = "idx_translations_auto_translated", columnList = "is_auto_translated"),
        @Index(name = "idx_translations_deleted_at", columnList = "deleted_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Translation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "translation_key_id", nullable = false, foreignKey = @ForeignKey(name = "fk_translations_key"))
    private TranslationKey translationKey;

    @Column(name = "locale_code", nullable = false, length = 10)
    private String localeCode;

    @Column(name = "translated_text", nullable = false, columnDefinition = "TEXT")
    private String translatedText;

    @Column(name = "is_auto_translated", nullable = false)
    @Builder.Default
    private Boolean isAutoTranslated = false;

    @Column(name = "quality_score")
    private Integer qualityScore;

    @Column(name = "reviewed_by", length = 100)
    private String reviewedBy;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (updatedAt == null) {
            updatedAt = Instant.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void softDelete() {
        this.deletedAt = Instant.now();
    }

    public boolean isReviewed() {
        return reviewedAt != null && reviewedBy != null;
    }
}
