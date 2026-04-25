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
 * KycDocument entity representing KYC verification documents.
 * Stores document metadata with encrypted sensitive information.
 * Actual document files are stored in object storage (S3/MinIO).
 */
@Entity
@Table(name = "kyc_document")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class KycDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 50)
    private DocumentType documentType;

    @Column(name = "document_number", nullable = false, length = 100)
    private String documentNumber;

    @Column(name = "document_number_encrypted", nullable = false, columnDefinition = "TEXT")
    private String documentNumberEncrypted;

    @Column(name = "issuing_country", nullable = false, length = 3)
    private String issuingCountry;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "file_path", nullable = false, columnDefinition = "TEXT")
    private String filePath;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_size_bytes", nullable = false)
    private Long fileSizeBytes;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    @Column(name = "verification_status", nullable = false, length = 20)
    @Builder.Default
    private String verificationStatus = "PENDING";

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(name = "verified_by")
    private UUID verifiedBy;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "metadata", columnDefinition = "JSONB")
    private String metadata;

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
     * Checks if the document is soft deleted
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * Soft deletes the document
     */
    public void delete() {
        this.deletedAt = Instant.now();
    }

    /**
     * Checks if the document is verified
     */
    public boolean isVerified() {
        return "VERIFIED".equals(verificationStatus);
    }

    /**
     * Checks if the document is pending verification
     */
    public boolean isPending() {
        return "PENDING".equals(verificationStatus) || "IN_REVIEW".equals(verificationStatus);
    }

    /**
     * Checks if the document is expired
     */
    public boolean isExpired() {
        if (expiryDate == null) {
            return false;
        }
        return LocalDate.now().isAfter(expiryDate);
    }

    /**
     * Marks document as verified
     */
    public void verify(UUID verifiedByUserId) {
        this.verificationStatus = "VERIFIED";
        this.verifiedAt = Instant.now();
        this.verifiedBy = verifiedByUserId;
        this.rejectionReason = null;
    }

    /**
     * Marks document as rejected
     */
    public void reject(String reason) {
        this.verificationStatus = "REJECTED";
        this.rejectionReason = reason;
        this.verifiedAt = null;
        this.verifiedBy = null;
    }

    /**
     * Marks document as expired
     */
    public void markExpired() {
        this.verificationStatus = "EXPIRED";
    }

    /**
     * Checks if document needs renewal (expires within 30 days)
     */
    public boolean needsRenewal() {
        if (expiryDate == null) {
            return false;
        }
        LocalDate thirtyDaysFromNow = LocalDate.now().plusDays(30);
        return expiryDate.isBefore(thirtyDaysFromNow);
    }
}
