-- liquibase formatted sql

-- changeset document-ingestion:4
-- comment: Seed reference data for document types and processing statuses

-- This migration is intentionally empty as document types and processing statuses
-- are managed as enums in the application code rather than database reference tables.
-- 
-- Document Types (managed in code):
-- - INVOICE
-- - RECEIPT
-- - STATEMENT
-- - CONTRACT
-- - IDENTITY_DOCUMENT
-- - TAX_DOCUMENT
-- - OTHER
--
-- Processing Statuses (managed in code):
-- - PENDING
-- - PROCESSING
-- - COMPLETED
-- - FAILED
--
-- If future requirements demand database-managed reference data,
-- this migration can be updated to create and populate reference tables.

-- rollback (no-op)
