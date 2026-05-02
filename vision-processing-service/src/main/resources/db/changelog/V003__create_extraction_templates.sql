-- liquibase formatted sql

-- changeset vision:3
-- comment: Create extraction_templates table

CREATE TABLE extraction_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    document_type VARCHAR(50) NOT NULL UNIQUE,
    template_name VARCHAR(100) NOT NULL,
    extraction_rules JSONB NOT NULL,
    validation_rules JSONB,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_by UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE extraction_templates IS 'Stores extraction templates for structured data extraction';
COMMENT ON COLUMN extraction_templates.id IS 'Unique template identifier';
COMMENT ON COLUMN extraction_templates.document_type IS 'Document type this template applies to';
COMMENT ON COLUMN extraction_templates.template_name IS 'Human-readable template name';
COMMENT ON COLUMN extraction_templates.extraction_rules IS 'JSON rules for data extraction (regex patterns, field mappings)';
COMMENT ON COLUMN extraction_templates.validation_rules IS 'JSON rules for validating extracted data';
COMMENT ON COLUMN extraction_templates.is_active IS 'Whether template is currently active';
COMMENT ON COLUMN extraction_templates.created_by IS 'Admin user who created the template';
COMMENT ON COLUMN extraction_templates.created_at IS 'Template creation timestamp';
COMMENT ON COLUMN extraction_templates.updated_at IS 'Last update timestamp';
COMMENT ON COLUMN extraction_templates.deleted_at IS 'Soft delete timestamp';
COMMENT ON COLUMN extraction_templates.version IS 'Optimistic locking version';

-- rollback DROP TABLE extraction_templates;
