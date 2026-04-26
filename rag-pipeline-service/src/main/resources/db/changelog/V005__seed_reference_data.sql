-- liquibase formatted sql

-- changeset rag-pipeline:5
-- comment: Seed reference data for RAG pipeline service

-- Enable pgvector extension if not already enabled
CREATE EXTENSION IF NOT EXISTS vector;

-- No reference data needed for RAG pipeline service
-- This file is included for consistency with other services
-- Future reference data can be added here

-- rollback DROP EXTENSION IF EXISTS vector;
