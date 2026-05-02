-- liquibase formatted sql

-- changeset liquibase:5
-- comment: Seed reference data for supported locales and common translation keys

-- Insert supported locales
INSERT INTO supported_locales (locale_code, language_name, native_name, is_rtl, is_active, display_order) VALUES
('en', 'English', 'English', FALSE, TRUE, 1),
('es', 'Spanish', 'Español', FALSE, TRUE, 2),
('fr', 'French', 'Français', FALSE, TRUE, 3),
('de', 'German', 'Deutsch', FALSE, TRUE, 4),
('hi', 'Hindi', 'हिन्दी', FALSE, TRUE, 5),
('ar', 'Arabic', 'العربية', TRUE, TRUE, 6),
('zh', 'Chinese', '中文', FALSE, TRUE, 7);

-- Insert common translation keys
INSERT INTO translation_keys (key_name, category, description, default_value, is_dynamic) VALUES
('welcome.message', 'ui', 'Welcome message on homepage', 'Welcome to Banking Platform', FALSE),
('login.button', 'ui', 'Login button text', 'Login', FALSE),
('logout.button', 'ui', 'Logout button text', 'Logout', FALSE),
('account.balance', 'ui', 'Account balance label', 'Account Balance', FALSE),
('transaction.history', 'ui', 'Transaction history label', 'Transaction History', FALSE),
('error.generic', 'error', 'Generic error message', 'An error occurred. Please try again.', FALSE),
('error.unauthorized', 'error', 'Unauthorized access error', 'You are not authorized to access this resource.', FALSE),
('success.transaction', 'notification', 'Transaction success message', 'Transaction completed successfully', FALSE),
('email.welcome.subject', 'email', 'Welcome email subject', 'Welcome to Banking Platform', FALSE),
('sms.otp.message', 'sms', 'OTP SMS message', 'Your OTP is: {code}. Valid for 5 minutes.', TRUE);

-- Insert English translations (default)
INSERT INTO translations (translation_key_id, locale_code, translated_value, is_auto_translated, translation_source)
SELECT id, 'en', default_value, FALSE, 'manual'
FROM translation_keys;

-- rollback DELETE FROM translations WHERE locale_code = 'en';
-- rollback DELETE FROM translation_keys;
-- rollback DELETE FROM supported_locales;
