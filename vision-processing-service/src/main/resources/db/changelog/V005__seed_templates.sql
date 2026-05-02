-- liquibase formatted sql

-- changeset vision:5
-- comment: Seed default extraction templates

-- Receipt template
INSERT INTO extraction_templates (document_type, template_name, extraction_rules, validation_rules, is_active)
VALUES (
    'RECEIPT',
    'Standard Receipt Template',
    '{
        "merchant": {"pattern": "^(.+)$", "line": 1},
        "total": {"pattern": "total[:\\s]*\\$?([0-9]+\\.[0-9]{2})", "flags": "i"},
        "date": {"pattern": "(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})"},
        "tax": {"pattern": "tax[:\\s]*\\$?([0-9]+\\.[0-9]{2})", "flags": "i"}
    }'::jsonb,
    '{
        "required_fields": ["merchant", "total"],
        "total": {"min": 0, "max": 999999.99}
    }'::jsonb,
    true
);

-- Invoice template
INSERT INTO extraction_templates (document_type, template_name, extraction_rules, validation_rules, is_active)
VALUES (
    'INVOICE',
    'Standard Invoice Template',
    '{
        "invoice_number": {"pattern": "invoice\\s*#?[:\\s]*([A-Z0-9-]+)", "flags": "i"},
        "vendor": {"pattern": "from[:\\s]*(.+)", "flags": "i"},
        "total": {"pattern": "total[:\\s]*\\$?([0-9]+\\.[0-9]{2})", "flags": "i"},
        "due_date": {"pattern": "due[:\\s]*(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})", "flags": "i"}
    }'::jsonb,
    '{
        "required_fields": ["invoice_number", "vendor", "total"]
    }'::jsonb,
    true
);

-- Check template
INSERT INTO extraction_templates (document_type, template_name, extraction_rules, validation_rules, is_active)
VALUES (
    'CHECK',
    'Standard Check Template',
    '{
        "routing_number": {"pattern": "\\b([0-9]{9})\\b"},
        "account_number": {"pattern": "\\b([0-9]{8,17})\\b"},
        "check_number": {"pattern": "check\\s*#?[:\\s]*([0-9]+)", "flags": "i"},
        "amount": {"pattern": "\\$([0-9]+\\.[0-9]{2})"},
        "payee": {"pattern": "pay to the order of[:\\s]*(.+)", "flags": "i"}
    }'::jsonb,
    '{
        "required_fields": ["routing_number", "account_number", "amount", "payee"],
        "routing_number": {"length": 9},
        "account_number": {"min_length": 8, "max_length": 17}
    }'::jsonb,
    true
);

-- Bank Statement template
INSERT INTO extraction_templates (document_type, template_name, extraction_rules, validation_rules, is_active)
VALUES (
    'BANK_STATEMENT',
    'Standard Bank Statement Template',
    '{
        "account_number": {"pattern": "account\\s*#?[:\\s]*([0-9X-]+)", "flags": "i"},
        "statement_date": {"pattern": "statement date[:\\s]*(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})", "flags": "i"},
        "beginning_balance": {"pattern": "beginning balance[:\\s]*\\$?([0-9,]+\\.[0-9]{2})", "flags": "i"},
        "ending_balance": {"pattern": "ending balance[:\\s]*\\$?([0-9,]+\\.[0-9]{2})", "flags": "i"}
    }'::jsonb,
    '{
        "required_fields": ["account_number", "statement_date"]
    }'::jsonb,
    true
);

-- ID Document template
INSERT INTO extraction_templates (document_type, template_name, extraction_rules, validation_rules, is_active)
VALUES (
    'ID_DOCUMENT',
    'Standard ID Document Template',
    '{
        "full_name": {"pattern": "name[:\\s]*(.+)", "flags": "i"},
        "date_of_birth": {"pattern": "dob|date of birth[:\\s]*(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})", "flags": "i"},
        "id_number": {"pattern": "id|license\\s*#?[:\\s]*([A-Z0-9-]+)", "flags": "i"},
        "expiration_date": {"pattern": "exp|expires[:\\s]*(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})", "flags": "i"}
    }'::jsonb,
    '{
        "required_fields": ["full_name", "date_of_birth", "id_number"]
    }'::jsonb,
    true
);

-- Generic template
INSERT INTO extraction_templates (document_type, template_name, extraction_rules, validation_rules, is_active)
VALUES (
    'GENERIC',
    'Generic Document Template',
    '{
        "full_text": {"extract": "all"}
    }'::jsonb,
    '{}'::jsonb,
    true
);

-- rollback DELETE FROM extraction_templates WHERE document_type IN ('RECEIPT', 'INVOICE', 'CHECK', 'BANK_STATEMENT', 'ID_DOCUMENT', 'GENERIC');
