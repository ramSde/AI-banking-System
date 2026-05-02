-- liquibase formatted sql

-- changeset vision:4
-- comment: Create indexes for performance optimization

-- Indexes on vision_documents
CREATE INDEX idx_vision_documents_user_id ON vision_documents(user_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_vision_documents_status ON vision_documents(processing_status) WHERE deleted_at IS NULL;
CREATE INDEX idx_vision_documents_type ON vision_documents(document_type) WHERE deleted_at IS NULL;
CREATE INDEX idx_vision_documents_created ON vision_documents(created_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX idx_vision_documents_user_type ON vision_documents(user_id, document_type) WHERE deleted_at IS NULL;
CREATE INDEX idx_vision_documents_user_status ON vision_documents(user_id, processing_status) WHERE deleted_at IS NULL;
CREATE INDEX idx_vision_documents_user_created ON vision_documents(user_id, created_at DESC) WHERE deleted_at IS NULL;

-- Indexes on ocr_results
CREATE INDEX idx_ocr_results_document_id ON ocr_results(document_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_ocr_results_document_page ON ocr_results(document_id, page_number) WHERE deleted_at IS NULL;

-- Indexes on extraction_templates
CREATE INDEX idx_extraction_templates_type ON extraction_templates(document_type) WHERE deleted_at IS NULL;
CREATE INDEX idx_extraction_templates_active ON extraction_templates(is_active) WHERE deleted_at IS NULL;

-- rollback DROP INDEX IF EXISTS idx_vision_documents_user_id;
-- rollback DROP INDEX IF EXISTS idx_vision_documents_status;
-- rollback DROP INDEX IF EXISTS idx_vision_documents_type;
-- rollback DROP INDEX IF EXISTS idx_vision_documents_created;
-- rollback DROP INDEX IF EXISTS idx_vision_documents_user_type;
-- rollback DROP INDEX IF EXISTS idx_vision_documents_user_status;
-- rollback DROP INDEX IF EXISTS idx_vision_documents_user_created;
-- rollback DROP INDEX IF EXISTS idx_ocr_results_document_id;
-- rollback DROP INDEX IF EXISTS idx_ocr_results_document_page;
-- rollback DROP INDEX IF EXISTS idx_extraction_templates_type;
-- rollback DROP INDEX IF EXISTS idx_extraction_templates_active;
