--liquibase formatted sql

--changeset user-service:4
--comment: Seed reference data for user service

-- This migration is intentionally empty as user service does not require seed data
-- All user data is created dynamically through API calls
-- Reference data like countries, currencies, languages are managed by external services

-- Future seed data can be added here if needed:
-- - Default user preferences templates
-- - System users (if required)
-- - Configuration data

--rollback -- No rollback needed for empty seed data
