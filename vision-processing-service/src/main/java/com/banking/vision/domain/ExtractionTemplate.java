package com.banking.vision.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Extraction Template entity defining rules for structured data extraction.
 * 
 * Each document type has a template with:
 * - Extraction rules (regex patterns, field mappings)
 * - Validation rules (required fields, format validation)
 * 
 * Templates are managed by admins and used by the extraction service.
 */
@Entity
@Table(name = "extraction_templates")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExtractionTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, unique = true, length = 50)
    private DocumentType documentType;

    @Column(name = "template_name", nullable = false, length = 100)
    private String templateName;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "extraction_rules", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> extractionRules;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "validation_rules", columnDefinition = "jsonb")
    private Map<String, Object> validationRules;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_by")
    private UUID createdBy;

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
     * Activate the template.
     */
    public void activate() {
        this.isActive = true;
    }

    /**
     * Deactivate the template.
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * Soft delete the template.
     */
    public void softDelete() {
        this.deletedAt = Instant.now();
        this.isActive = false;
    }
}
