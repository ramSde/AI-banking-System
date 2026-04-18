-- liquibase formatted sql

-- changeset risk-service:4
-- comment: Seed default risk rules

-- New Device Rule
INSERT INTO risk_rule (id, name, description, rule_type, condition, risk_score_impact, enabled, priority)
VALUES (
    gen_random_uuid(),
    'New Device Detection',
    'Detects when a user logs in from a device not seen in the last 30 days',
    'DEVICE',
    '{"lookback_days": 30}'::jsonb,
    25,
    true,
    100
);

-- New Location Rule
INSERT INTO risk_rule (id, name, description, rule_type, condition, risk_score_impact, enabled, priority)
VALUES (
    gen_random_uuid(),
    'New Location Detection',
    'Detects when a user logs in from a new geographic location',
    'LOCATION',
    '{"distance_threshold_km": 100}'::jsonb,
    20,
    true,
    90
);

-- Velocity Rule
INSERT INTO risk_rule (id, name, description, rule_type, condition, risk_score_impact, enabled, priority)
VALUES (
    gen_random_uuid(),
    'Login Velocity Check',
    'Detects multiple login attempts in a short time period',
    'VELOCITY',
    '{"max_attempts": 3, "time_window_minutes": 15}'::jsonb,
    15,
    true,
    80
);

-- Time of Day Rule
INSERT INTO risk_rule (id, name, description, rule_type, condition, risk_score_impact, enabled, priority)
VALUES (
    gen_random_uuid(),
    'Unusual Time Detection',
    'Detects logins at unusual hours for the user',
    'TIME',
    '{"unusual_hours_start": 2, "unusual_hours_end": 6}'::jsonb,
    10,
    true,
    70
);

-- Failed Attempts Rule
INSERT INTO risk_rule (id, name, description, rule_type, condition, risk_score_impact, enabled, priority)
VALUES (
    gen_random_uuid(),
    'Failed Login Attempts',
    'Tracks recent failed login attempts',
    'FAILED_ATTEMPTS',
    '{"max_failed_attempts": 3, "lookback_minutes": 30}'::jsonb,
    30,
    true,
    110
);

-- rollback DELETE FROM risk_rule WHERE rule_type IN ('DEVICE', 'LOCATION', 'VELOCITY', 'TIME', 'FAILED_ATTEMPTS');
