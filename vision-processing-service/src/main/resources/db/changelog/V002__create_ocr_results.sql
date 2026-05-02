-- liquibase formatted sql

-- changeset vision:2
-- comment: Create ocr_results table

CREATE TABLE ocr_results (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    document_id UUID NOT NULL REFERENCES vision_documents(id) ON DELETE CASCADE,
    raw_text TEXT NOT NULL,
    confidence_score DECIMAL(5,2),
    language_detected VARCHAR(10),
    page_number INT NOT NULL DEFAULT 1,
    processing_time_ms BIGINT,
    ocr_engine VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE ocr_results IS 'Stores OCR text extraction results per page';
COMMENT ON COLUMN ocr_results.id IS 'Unique OCR result identifier';
COMMENT ON COLUMN ocr_results.document_id IS 'Reference to parent document';
COMMENT ON COLUMN ocr_results.raw_text IS 'Raw extracted text from OCR';
COMMENT ON COLUMN ocr_results.confidence_score IS 'OCR confidence score for this page (0-100)';
COMMENT ON COLUMN ocr_results.language_detected IS 'Detected language code (ISO 639-1)';
COMMENT ON COLUMN ocr_results.page_number IS 'Page number (1-indexed)';
COMMENT ON COLUMN ocr_results.processing_time_ms IS 'OCR processing time in milliseconds';
COMMENT ON COLUMN ocr_results.ocr_engine IS 'OCR engine used (e.g., Tesseract)';
COMMENT ON COLUMN ocr_results.created_at IS 'OCR result creation timestamp';
COMMENT ON COLUMN ocr_results.updated_at IS 'Last update timestamp';
COMMENT ON COLUMN ocr_results.deleted_at IS 'Soft delete timestamp';
COMMENT ON COLUMN ocr_results.version IS 'Optimistic locking version';

-- rollback DROP TABLE ocr_results;
