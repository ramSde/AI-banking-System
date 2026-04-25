--liquibase formatted sql

--changeset transaction-service:3
--comment: Create idempotency_keys table for duplicate prevention

CREATE TABLE idempotency_keys (
    idempotency_key VARCHAR(255) PRIMARY KEY,
    transaction_id UUID NOT NULL,
    request_hash VARCHAR(64) NOT NULL,
    response_body JSONB NOT NULL,
    response_status INT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMPTZ NOT NULL
);

COMMENT ON TABLE idempotency_keys IS 'Idempotency key storage for duplicate request prevention';
COMMENT ON COLUMN idempotency_keys.idempotency_key IS 'Unique idempotency key from request header';
COMMENT ON COLUMN idempotency_keys.transaction_id IS 'Associated transaction ID';
COMMENT ON COLUMN idempotency_keys.request_hash IS 'SHA-256 hash of request body';
COMMENT ON COLUMN idempotency_keys.response_body IS 'Cached response body';
COMMENT ON COLUMN idempotency_keys.response_status IS 'HTTP response status code';
COMMENT ON COLUMN idempotency_keys.expires_at IS 'Expiration timestamp (24 hours default)';

--rollback DROP TABLE idempotency_keys;
