--liquibase formatted sql

--changeset user-service:3
--comment: Create kyc_document table for KYC verification documents

CREATE TABLE IF NOT EXISTS kyc_document (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    document_type VARCHAR(50) NOT NULL,
    document_number VARCHAR(100) NOT NULL,
    document_number_encrypted TEXT NOT NULL,
    issuing_country VARCHAR(3) NOT NULL,
    issue_date DATE NOT NULL,
    expiry_date DATE,
    file_path TEXT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    verification_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    verified_at TIMESTAMPTZ,
    verified_by UUID,
    rejection_reason TEXT,
    metadata JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_kyc_document_user FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE,
    CONSTRAINT chk_document_type CHECK (document_type IN ('PASSPORT', 'NATIONAL_ID', 'DRIVERS_LICENSE', 'RESIDENCE_PERMIT', 'VOTER_ID', 'PAN_CARD', 'AADHAAR_CARD')),
    CONSTRAINT chk_issuing_country CHECK (issuing_country ~ '^[A-Z]{2,3}$'),
    CONSTRAINT chk_verification_status CHECK (verification_status IN ('PENDING', 'IN_REVIEW', 'VERIFIED', 'REJECTED', 'EXPIRED')),
    CONSTRAINT chk_file_size CHECK (file_size_bytes > 0 AND file_size_bytes <= 10485760),
    CONSTRAINT chk_expiry_date CHECK (expiry_date IS NULL OR expiry_date > issue_date)
);

COMMENT ON TABLE kyc_document IS 'KYC verification documents with encrypted sensitive data';
COMMENT ON COLUMN kyc_document.id IS 'Unique document identifier';
COMMENT ON COLUMN kyc_document.user_id IS 'Reference to user table';
COMMENT ON COLUMN kyc_document.document_type IS 'Type of identity document';
COMMENT ON COLUMN kyc_document.document_number IS 'Masked document number for display';
COMMENT ON COLUMN kyc_document.document_number_encrypted IS 'Encrypted document number (AES-256-GCM)';
COMMENT ON COLUMN kyc_document.issuing_country IS 'Country code (ISO 3166-1 alpha-2 or alpha-3)';
COMMENT ON COLUMN kyc_document.issue_date IS 'Document issue date';
COMMENT ON COLUMN kyc_document.expiry_date IS 'Document expiry date (null for non-expiring documents)';
COMMENT ON COLUMN kyc_document.file_path IS 'Object storage path (S3/MinIO key)';
COMMENT ON COLUMN kyc_document.file_name IS 'Original file name';
COMMENT ON COLUMN kyc_document.file_size_bytes IS 'File size in bytes (max 10MB)';
COMMENT ON COLUMN kyc_document.mime_type IS 'File MIME type';
COMMENT ON COLUMN kyc_document.verification_status IS 'Document verification status';
COMMENT ON COLUMN kyc_document.verified_at IS 'Timestamp when document was verified';
COMMENT ON COLUMN kyc_document.verified_by IS 'Admin user ID who verified the document';
COMMENT ON COLUMN kyc_document.rejection_reason IS 'Reason for rejection if status is REJECTED';
COMMENT ON COLUMN kyc_document.metadata IS 'Additional metadata (OCR results, confidence scores, etc.)';
COMMENT ON COLUMN kyc_document.version IS 'Optimistic locking version';

-- Indexes
CREATE INDEX idx_kyc_document_user_id ON kyc_document(user_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_kyc_document_type ON kyc_document(document_type) WHERE deleted_at IS NULL;
CREATE INDEX idx_kyc_document_status ON kyc_document(verification_status) WHERE deleted_at IS NULL;
CREATE INDEX idx_kyc_document_country ON kyc_document(issuing_country) WHERE deleted_at IS NULL;
CREATE INDEX idx_kyc_document_expiry ON kyc_document(expiry_date) WHERE deleted_at IS NULL AND expiry_date IS NOT NULL;
CREATE INDEX idx_kyc_document_created_at ON kyc_document(created_at);
CREATE INDEX idx_kyc_document_deleted_at ON kyc_document(deleted_at) WHERE deleted_at IS NOT NULL;

-- Composite index for common queries
CREATE INDEX idx_kyc_document_user_status ON kyc_document(user_id, verification_status) WHERE deleted_at IS NULL;

--rollback DROP TABLE IF EXISTS kyc_document;
