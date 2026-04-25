--liquibase formatted sql

--changeset fraud-detection:6
--comment: Seed default fraud detection rules

-- System user UUID for default rules
-- Using a fixed UUID for system-created rules
INSERT INTO fraud_rule (id, rule_name, rule_type, description, rule_config, weight, enabled, created_by)
VALUES
    (
        'a0000000-0000-0000-0000-000000000001',
        'High Velocity Transaction Check',
        'VELOCITY',
        'Detects unusually high transaction frequency within a time window',
        '{"maxTransactions": 10, "windowMinutes": 60, "scoreContribution": 25}',
        25,
        TRUE,
        '00000000-0000-0000-0000-000000000000'
    ),
    (
        'a0000000-0000-0000-0000-000000000002',
        'Large Amount Transaction',
        'AMOUNT',
        'Flags transactions exceeding large amount threshold',
        '{"threshold": 10000.00, "scoreContribution": 20}',
        20,
        TRUE,
        '00000000-0000-0000-0000-000000000000'
    ),
    (
        'a0000000-0000-0000-0000-000000000003',
        'Suspicious Amount Pattern',
        'AMOUNT',
        'Detects suspicious round amounts or patterns',
        '{"threshold": 50000.00, "scoreContribution": 30}',
        30,
        TRUE,
        '00000000-0000-0000-0000-000000000000'
    ),
    (
        'a0000000-0000-0000-0000-000000000004',
        'Geographic Anomaly Detection',
        'GEOGRAPHIC',
        'Detects transactions from unusual or high-risk locations',
        '{"maxDistanceKm": 500, "timeWindowHours": 1, "scoreContribution": 25}',
        25,
        TRUE,
        '00000000-0000-0000-0000-000000000000'
    ),
    (
        'a0000000-0000-0000-0000-000000000005',
        'Unusual Time Pattern',
        'TIME_PATTERN',
        'Flags transactions during unusual hours for the user',
        '{"unusualHoursStart": 2, "unusualHoursEnd": 5, "scoreContribution": 15}',
        15,
        TRUE,
        '00000000-0000-0000-0000-000000000000'
    ),
    (
        'a0000000-0000-0000-0000-000000000006',
        'New Account Risk',
        'ACCOUNT_AGE',
        'Higher risk for transactions from newly created accounts',
        '{"minAccountAgeDays": 7, "scoreContribution": 20}',
        20,
        TRUE,
        '00000000-0000-0000-0000-000000000000'
    ),
    (
        'a0000000-0000-0000-0000-000000000007',
        'Multiple Failed Attempts',
        'FAILED_ATTEMPTS',
        'Detects multiple failed transaction attempts',
        '{"maxFailedAttempts": 3, "windowMinutes": 30, "scoreContribution": 30}',
        30,
        TRUE,
        '00000000-0000-0000-0000-000000000000'
    );

--rollback DELETE FROM fraud_rule WHERE created_by = '00000000-0000-0000-0000-000000000000';
