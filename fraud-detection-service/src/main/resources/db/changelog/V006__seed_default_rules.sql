-- Seed Default Fraud Detection Rules

-- Velocity Check Rule
INSERT INTO fraud_rule (id, rule_name, rule_type, description, rule_config, weight, enabled, created_by)
VALUES (
    gen_random_uuid(),
    'High Velocity Transaction Check',
    'VELOCITY',
    'Detects unusually high transaction frequency',
    '{"maxTransactions": 10, "windowMinutes": 60, "riskScore": 40}',
    30,
    true,
    '00000000-0000-0000-0000-000000000000'
);

-- Large Amount Rule
INSERT INTO fraud_rule (id, rule_name, rule_type, description, rule_config, weight, enabled, created_by)
VALUES (
    gen_random_uuid(),
    'Large Transaction Amount Check',
    'AMOUNT',
    'Flags transactions above threshold',
    '{"threshold": 10000.00, "riskScore": 25}',
    25,
    true,
    '00000000-0000-0000-0000-000000000000'
);

-- Suspicious Amount Rule
INSERT INTO fraud_rule (id, rule_name, rule_type, description, rule_config, weight, enabled, created_by)
VALUES (
    gen_random_uuid(),
    'Suspicious Amount Pattern Check',
    'AMOUNT',
    'Detects suspicious amount patterns',
    '{"threshold": 50000.00, "riskScore": 50}',
    40,
    true,
    '00000000-0000-0000-0000-000000000000'
);

-- Time Pattern Rule
INSERT INTO fraud_rule (id, rule_name, rule_type, description, rule_config, weight, enabled, created_by)
VALUES (
    gen_random_uuid(),
    'Unusual Time Pattern Check',
    'TIME_PATTERN',
    'Detects transactions at unusual hours',
    '{"unusualHoursStart": 2, "unusualHoursEnd": 5, "riskScore": 15}',
    15,
    true,
    '00000000-0000-0000-0000-000000000000'
);

-- Account Age Rule
INSERT INTO fraud_rule (id, rule_name, rule_type, description, rule_config, weight, enabled, created_by)
VALUES (
    gen_random_uuid(),
    'New Account Risk Check',
    'ACCOUNT_AGE',
    'Higher risk for new accounts',
    '{"minAgeDays": 30, "riskScore": 20}',
    20,
    true,
    '00000000-0000-0000-0000-000000000000'
);

-- Rollback
--rollback DELETE FROM fraud_rule WHERE created_by = '00000000-0000-0000-0000-000000000000';
