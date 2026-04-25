--liquibase formatted sql

--changeset notification-service:4
--comment: Seed default notification templates

-- Welcome Email Template
INSERT INTO notification_templates (template_code, template_name, channel, subject, body_template, template_variables, locale, is_active)
VALUES (
    'WELCOME_EMAIL',
    'Welcome Email',
    'EMAIL',
    'Welcome to Banking Platform',
    '<html><body><h1>Welcome [[${userName}]]!</h1><p>Thank you for joining Banking Platform. Your account has been successfully created.</p><p>Account Number: [[${accountNumber}]]</p></body></html>',
    '["userName", "accountNumber"]'::jsonb,
    'en',
    true
);

-- OTP SMS Template
INSERT INTO notification_templates (template_code, template_name, channel, subject, body_template, template_variables, locale, is_active)
VALUES (
    'OTP_SMS',
    'OTP SMS',
    'SMS',
    NULL,
    'Your Banking Platform OTP is: [[${otpCode}]]. Valid for 5 minutes. Do not share this code.',
    '["otpCode"]'::jsonb,
    'en',
    true
);

-- Transaction Alert Email
INSERT INTO notification_templates (template_code, template_name, channel, subject, body_template, template_variables, locale, is_active)
VALUES (
    'TRANSACTION_ALERT_EMAIL',
    'Transaction Alert Email',
    'EMAIL',
    'Transaction Alert: [[${transactionType}]]',
    '<html><body><h2>Transaction Alert</h2><p>Dear [[${userName}]],</p><p>A [[${transactionType}]] transaction of [[${amount}]] [[${currency}]] was processed on your account.</p><p>Transaction ID: [[${transactionId}]]</p><p>Date: [[${transactionDate}]]</p><p>Balance: [[${balance}]] [[${currency}]]</p></body></html>',
    '["userName", "transactionType", "amount", "currency", "transactionId", "transactionDate", "balance"]'::jsonb,
    'en',
    true
);

-- Fraud Alert Push Notification
INSERT INTO notification_templates (template_code, template_name, channel, subject, body_template, template_variables, locale, is_active)
VALUES (
    'FRAUD_ALERT_PUSH',
    'Fraud Alert Push',
    'PUSH',
    'Security Alert',
    'Suspicious activity detected on your account. Transaction of [[${amount}]] [[${currency}]] was blocked. If this was you, please contact support.',
    '["amount", "currency"]'::jsonb,
    'en',
    true
);

-- Password Reset Email
INSERT INTO notification_templates (template_code, template_name, channel, subject, body_template, template_variables, locale, is_active)
VALUES (
    'PASSWORD_RESET_EMAIL',
    'Password Reset Email',
    'EMAIL',
    'Password Reset Request',
    '<html><body><h2>Password Reset</h2><p>Dear [[${userName}]],</p><p>We received a request to reset your password. Click the link below to reset:</p><p><a href="[[${resetLink}]]">Reset Password</a></p><p>This link expires in 1 hour.</p><p>If you did not request this, please ignore this email.</p></body></html>',
    '["userName", "resetLink"]'::jsonb,
    'en',
    true
);

-- Account Statement Ready Email
INSERT INTO notification_templates (template_code, template_name, channel, subject, body_template, template_variables, locale, is_active)
VALUES (
    'STATEMENT_READY_EMAIL',
    'Statement Ready Email',
    'EMAIL',
    'Your Account Statement is Ready',
    '<html><body><h2>Account Statement</h2><p>Dear [[${userName}]],</p><p>Your account statement for [[${period}]] is now available.</p><p><a href="[[${downloadLink}]]">Download Statement</a></p><p>This link expires in 7 days.</p></body></html>',
    '["userName", "period", "downloadLink"]'::jsonb,
    'en',
    true
);

-- Login Alert SMS
INSERT INTO notification_templates (template_code, template_name, channel, subject, body_template, template_variables, locale, is_active)
VALUES (
    'LOGIN_ALERT_SMS',
    'Login Alert SMS',
    'SMS',
    NULL,
    'New login to your Banking Platform account from [[${deviceName}]] at [[${loginTime}]]. If this was not you, contact support immediately.',
    '["deviceName", "loginTime"]'::jsonb,
    'en',
    true
);

-- Low Balance Alert Push
INSERT INTO notification_templates (template_code, template_name, channel, subject, body_template, template_variables, locale, is_active)
VALUES (
    'LOW_BALANCE_ALERT_PUSH',
    'Low Balance Alert Push',
    'PUSH',
    'Low Balance Alert',
    'Your account balance is low: [[${balance}]] [[${currency}]]. Consider adding funds to avoid overdraft fees.',
    '["balance", "currency"]'::jsonb,
    'en',
    true
);

--rollback DELETE FROM notification_templates WHERE template_code IN ('WELCOME_EMAIL', 'OTP_SMS', 'TRANSACTION_ALERT_EMAIL', 'FRAUD_ALERT_PUSH', 'PASSWORD_RESET_EMAIL', 'STATEMENT_READY_EMAIL', 'LOGIN_ALERT_SMS', 'LOW_BALANCE_ALERT_PUSH');
