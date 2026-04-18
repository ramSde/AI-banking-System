# Identity Service

Production-grade identity and authentication service for the banking platform.

## Overview

The Identity Service is responsible for user registration, authentication, and JWT token management. It implements secure authentication patterns including:

- **JWT Access Tokens**: RS256-signed, 15-minute TTL
- **Refresh Token Rotation**: 7-day TTL with automatic rotation on use
- **BCrypt Password Hashing**: Cost factor 12 for secure password storage
- **Account Lockout**: Automatic lockout after failed login attempts
- **Audit Trail**: Complete token lifecycle tracking via Kafka events

## Bounded Context

This service owns the **Identity & Authentication** domain, managing:
- User identity (email, phone, username)
- Authentication credentials (password hashes)
- Token lifecycle (issuance, validation, rotation, revocation)
- Login attempt tracking and account lockout

## Prerequisites

- Java JDK 25
- Maven 3.9+
- Docker & Docker Compose
- PostgreSQL 16
- Redis 7
- Apache Kafka 3.5+

## Local Setup

### 1. Environment Configuration

Copy the example environment file and configure:

```bash
cp .env.example .env
```

Edit `.env` and set required values (database credentials, JWT keys, etc.)

### 2. Generate RSA Keys for JWT

```bash
# Generate private key
openssl genrsa -out private_key.pem 2048

# Extract public key
openssl rsa -in private_key.pem -pubout -out public_key.pem

# Convert to single-line format for .env
cat private_key.pem | tr '\n' '\\n'
cat public_key.pem | tr '\n' '\\n'
```

### 3. Start Infrastructure

```bash
cd infrastructure/docker
docker-compose up -d postgres redis kafka
```

### 4. Create Database

```bash
docker exec -it banking-postgres psql -U admin -c "CREATE DATABASE identity_db;"
```

### 5. Build and Run

```bash
mvn clean install
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

The service will start on port `8082` (application) and `8083` (management/actuator).

## API Endpoints

| Method | Path | Auth Required | Description |
|--------|------|---------------|-------------|
| POST | `/api/v1/auth/register` | No | Register new user |
| POST | `/api/v1/auth/login` | No | Authenticate and receive tokens |
| POST | `/api/v1/auth/refresh` | No | Refresh access token |
| POST | `/api/v1/auth/logout` | Yes | Revoke all user tokens |
| POST | `/api/v1/auth/revoke` | Yes | Revoke specific refresh token |
| POST | `/api/v1/password/change` | Yes | Change user password |

### Swagger UI

Access interactive API documentation at:
```
http://localhost:8082/api/swagger-ui.html
```

## Sample Requests

### Register User

```bash
curl -X POST http://localhost:8082/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "SecurePass123!",
    "phoneNumber": "+1234567890",
    "username": "johndoe"
  }'
```

### Login

```bash
curl -X POST http://localhost:8082/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "SecurePass123!",
    "deviceId": "device-uuid-12345"
  }'
```

### Refresh Token

```bash
curl -X POST http://localhost:8082/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "rt_abc123def456...",
    "deviceId": "device-uuid-12345"
  }'
```

### Change Password

```bash
curl -X POST "http://localhost:8082/api/v1/password/change?userId=<user-id>" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <access-token>" \
  -d '{
    "currentPassword": "SecurePass123!",
    "newPassword": "NewSecurePass456!"
  }'
```

## Environment Variables

| Variable | Default | Description | Required |
|----------|---------|-------------|----------|
| `SPRING_PROFILES_ACTIVE` | dev | Active Spring profile (dev/staging/prod) | Yes |
| `DB_HOST` | localhost | PostgreSQL host | Yes |
| `DB_PORT` | 5432 | PostgreSQL port | Yes |
| `DB_NAME` | identity_db | Database name | Yes |
| `DB_USERNAME` | admin | Database username | Yes |
| `DB_PASSWORD` | admin | Database password | Yes |
| `REDIS_HOST` | localhost | Redis host | Yes |
| `REDIS_PORT` | 6379 | Redis port | Yes |
| `KAFKA_BOOTSTRAP_SERVERS` | localhost:9092 | Kafka bootstrap servers | Yes |
| `JWT_PRIVATE_KEY` | - | RSA private key (PEM format) | Yes |
| `JWT_PUBLIC_KEY` | - | RSA public key (PEM format) | Yes |
| `JWT_ACCESS_TOKEN_TTL` | 15 | Access token TTL (minutes) | No |
| `JWT_REFRESH_TOKEN_TTL` | 7 | Refresh token TTL (days) | No |
| `PASSWORD_BCRYPT_STRENGTH` | 12 | BCrypt cost factor | No |
| `MAX_LOGIN_ATTEMPTS` | 5 | Max failed login attempts before lockout | No |
| `LOCKOUT_DURATION_MINUTES` | 30 | Account lockout duration | No |

## Architecture Decisions

### 1. RS256 JWT Signing
- **Decision**: Use RSA-256 asymmetric signing instead of HS256
- **Rationale**: Allows public key distribution for token validation without exposing signing key
- **Tradeoff**: Slightly slower than HMAC, but necessary for microservices architecture

### 2. Refresh Token Rotation
- **Decision**: Rotate refresh tokens on every use
- **Rationale**: Limits impact of token theft; detects token reuse attacks
- **Tradeoff**: More database writes, but significantly improves security

### 3. BCrypt for Passwords
- **Decision**: Use BCrypt with cost factor 12
- **Rationale**: Industry standard, adaptive hashing resistant to brute force
- **Tradeoff**: Slower than SHA-256, but intentional for security

### 4. Token Family Tracking
- **Decision**: Track token rotation chains via `token_family_id`
- **Rationale**: Enables detection of token reuse attacks and revocation of entire chain
- **Tradeoff**: Additional complexity, but critical for security

## Kafka Events Published

### Topic: `banking.identity.user-registered`
Published when a new user registers.

```json
{
  "eventId": "uuid",
  "eventType": "UserRegistered",
  "version": "1.0",
  "occurredAt": "2024-01-01T00:00:00Z",
  "correlationId": "uuid",
  "payload": {
    "userId": "uuid",
    "email": "user@example.com",
    "phoneNumber": "+1234567890",
    "username": "johndoe",
    "registeredAt": "2024-01-01T00:00:00Z"
  }
}
```

### Topic: `banking.identity.authentication`
Published for login, logout, and token refresh events.

```json
{
  "eventId": "uuid",
  "eventType": "UserLoggedIn",
  "version": "1.0",
  "occurredAt": "2024-01-01T00:00:00Z",
  "correlationId": "uuid",
  "payload": {
    "userId": "uuid",
    "email": "user@example.com",
    "eventAction": "LOGIN",
    "ipAddress": "192.168.1.1",
    "deviceId": "device-uuid",
    "userAgent": "Mozilla/5.0...",
    "success": true,
    "failureReason": null
  }
}
```

## Database Schema

### Tables
- **users**: Core user identity and status
- **credentials**: Password hashes and history
- **refresh_token_audit**: Token lifecycle tracking

See `src/main/resources/db/changelog/migrations/` for complete DDL.

## Health Checks

- **Liveness**: `GET /actuator/health/liveness`
- **Readiness**: `GET /actuator/health/readiness`
- **Metrics**: `GET /actuator/prometheus`

## Known Limitations

1. **Token Validation Performance**: Refresh token validation requires scanning all tokens (BCrypt comparison). Consider Redis caching for high-traffic scenarios.
2. **No Email Verification**: Email verification flow not yet implemented (planned for future release).
3. **No Password Reset**: Password reset via email not yet implemented (planned for future release).
4. **No MFA**: Multi-factor authentication not yet implemented (separate MFA service planned).

## Planned Improvements

1. Implement email verification flow
2. Add password reset via email/SMS
3. Add Redis caching for token validation
4. Implement rate limiting per IP/user
5. Add device fingerprinting integration
6. Implement password expiry policies
7. Add support for OAuth2/OIDC providers

## Security Considerations

- All passwords are hashed with BCrypt (cost factor 12)
- Refresh tokens are stored as BCrypt hashes
- JWT tokens use RS256 asymmetric signing
- Account lockout after 5 failed attempts (configurable)
- Token rotation on every refresh
- Token reuse detection with family revocation
- No sensitive data in logs (passwords, tokens masked)

## Support

For issues or questions, contact the platform team or create an issue in the repository.
