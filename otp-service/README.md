# OTP & MFA Service

Production-grade OTP (One-Time Password) and MFA (Multi-Factor Authentication) service for the banking platform.

## Overview

The OTP & MFA Service provides multi-factor authentication capabilities including:
- **TOTP (Time-based One-Time Password)**: RFC 6238 compliant, works with Google Authenticator, Authy, etc.
- **SMS/Email OTP**: 6-digit codes with 5-minute TTL
- **Backup Codes**: 8 recovery codes per user for account recovery
- **Redis-backed storage**: All OTP codes stored as BCrypt hashes with TTL
- **Rate limiting**: Prevents brute-force attacks
- **Kafka event publishing**: Audit trail for all MFA operations

## Bounded Context

**Domain**: Authentication & Security  
**Responsibility**: Multi-factor authentication and OTP management  
**Dependencies**: Identity Service (user validation), Notification Service (SMS/Email delivery)

## Prerequisites

- Java 25
- Maven 3.9+
- Docker & Docker Compose
- PostgreSQL 16
- Redis 7
- Apache Kafka 3.x

## Local Setup

### 1. Environment Configuration

Copy the example environment file:

```bash
cp .env.example .env
```

Edit `.env` and configure the required variables.

### 2. Start Infrastructure

```bash
# From project root
docker-compose up -d postgres redis kafka
```

### 3. Create Database

```bash
docker exec -it banking-postgres psql -U admin -c "CREATE DATABASE otp_db;"
```

### 4. Run the Service

```bash
# From project root
mvn clean install
mvn spring-boot:run -pl otp-service
```

The service will start on `http://localhost:8082/api`

### 5. Access Swagger UI

Open `http://localhost:8082/api/swagger-ui.html` to explore the API.

## API Endpoints

### MFA Management

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/v1/mfa/enrollments/{userId}` | Required | Get user MFA enrollments |
| GET | `/v1/mfa/status/{userId}` | Required | Check if user has active MFA |
| DELETE | `/v1/mfa/{userId}/{method}` | Required | Disable specific MFA method |
| DELETE | `/v1/mfa/{userId}` | Required | Disable all MFA methods |

### TOTP Operations

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/v1/otp/totp/enroll` | Required | Enroll in TOTP (returns QR code) |
| POST | `/v1/otp/totp/verify-enrollment` | Required | Verify TOTP code to complete enrollment |
| POST | `/v1/otp/totp/verify` | Required | Verify TOTP code for authentication |

### OTP Operations (SMS/Email)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/v1/otp/send` | Required | Send OTP via SMS or Email |
| POST | `/v1/otp/verify` | Required | Verify OTP code |

### Backup Codes

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/v1/backup-codes/generate/{userId}` | Required | Generate backup codes |
| POST | `/v1/backup-codes/verify` | Required | Verify backup code |
| GET | `/v1/backup-codes/remaining/{userId}` | Required | Get remaining backup codes count |
| POST | `/v1/backup-codes/regenerate/{userId}` | Required | Regenerate backup codes |

## Sample Requests

### 1. Enroll in TOTP

```bash
curl -X POST http://localhost:8082/api/v1/otp/totp/enroll \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "userId": "550e8400-e29b-41d4-a716-446655440000"
  }'
```

**Response:**
```json
{
  "success": true,
  "data": {
    "secret": "JBSWY3DPEHPK3PXP",
    "qrCode": "iVBORw0KGgoAAAANSUhEUgAA...",
    "manualEntryKey": "JBSW-Y3DP-EHPK-3PXP",
    "issuer": "BankingPlatform",
    "accountName": "550e8400-e29b-41d4-a716-446655440000"
  },
  "error": null,
  "traceId": "abc123",
  "timestamp": "2024-01-01T00:00:00Z"
}
```

### 2. Verify TOTP Enrollment

```bash
curl -X POST http://localhost:8082/api/v1/otp/totp/verify-enrollment \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "code": "123456"
  }'
```

### 3. Send SMS OTP

```bash
curl -X POST http://localhost:8082/api/v1/otp/send \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "method": "SMS",
    "phoneNumber": "+1234567890"
  }'
```

### 4. Verify OTP

```bash
curl -X POST http://localhost:8082/api/v1/otp/verify \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "method": "SMS",
    "code": "123456"
  }'
```

### 5. Generate Backup Codes

```bash
curl -X POST http://localhost:8082/api/v1/backup-codes/generate/550e8400-e29b-41d4-a716-446655440000 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:**
```json
{
  "success": true,
  "data": {
    "codes": [
      "ABCD-1234-EFGH",
      "IJKL-5678-MNOP",
      "QRST-9012-UVWX",
      "YZAB-3456-CDEF",
      "GHIJ-7890-KLMN",
      "OPQR-1234-STUV",
      "WXYZ-5678-ABCD",
      "EFGH-9012-IJKL"
    ],
    "count": 8,
    "warning": "Save these codes in a secure location. They will not be shown again."
  },
  "error": null,
  "traceId": "xyz789",
  "timestamp": "2024-01-01T00:00:00Z"
}
```

## Environment Variables

| Variable | Default | Description | Required |
|----------|---------|-------------|----------|
| `SERVER_PORT` | 8082 | HTTP server port | No |
| `DB_HOST` | localhost | PostgreSQL host | Yes |
| `DB_PORT` | 5432 | PostgreSQL port | Yes |
| `DB_NAME` | otp_db | Database name | Yes |
| `DB_USERNAME` | admin | Database username | Yes |
| `DB_PASSWORD` | admin | Database password | Yes |
| `REDIS_HOST` | localhost | Redis host | Yes |
| `REDIS_PORT` | 6379 | Redis port | Yes |
| `REDIS_PASSWORD` | | Redis password | No |
| `OTP_LENGTH` | 6 | OTP code length | No |
| `OTP_TTL_SECONDS` | 300 | OTP validity duration (5 min) | No |
| `OTP_MAX_ATTEMPTS` | 3 | Max OTP verification attempts | No |
| `TOTP_ISSUER` | BankingPlatform | TOTP issuer name | No |
| `TOTP_PERIOD_SECONDS` | 30 | TOTP time step | No |
| `TOTP_DIGITS` | 6 | TOTP code length | No |
| `BACKUP_CODE_COUNT` | 8 | Number of backup codes | No |
| `BCRYPT_STRENGTH` | 12 | BCrypt cost factor | No |
| `KAFKA_BOOTSTRAP_SERVERS` | localhost:9092 | Kafka brokers | Yes |

## Architecture Decisions

### 1. TOTP Implementation
- **RFC 6238 compliant**: Standard TOTP algorithm with 30-second time steps
- **Time step tolerance**: ±1 step (90 seconds total window) to account for clock skew
- **QR code generation**: Base64-encoded PNG for easy scanning

### 2. OTP Storage
- **BCrypt hashing**: All OTP codes stored as BCrypt hashes (cost factor 12)
- **Redis TTL**: Automatic expiration after 5 minutes
- **Attempt limiting**: Max 3 attempts per OTP, then code is invalidated

### 3. Rate Limiting
- **Per-user limit**: 5 OTP requests per 5-minute window
- **Sliding window**: Redis-based implementation
- **Prevents abuse**: Protects against brute-force and DoS attacks

### 4. Backup Codes
- **One-time use**: Each code can only be used once
- **BCrypt hashed**: Stored as hashes, never in plain text
- **Format**: XXXX-XXXX-XXXX (12 characters, alphanumeric)

### 5. Event Publishing
- **Kafka topics**: 
  - `banking.otp.mfa-enrolled`: MFA enrollment events
  - `banking.otp.otp-verified`: OTP verification events
  - `banking.otp.backup-code-used`: Backup code usage events
- **Audit trail**: All MFA operations tracked for security monitoring

## Security Considerations

1. **No plain text storage**: All OTP codes and backup codes stored as BCrypt hashes
2. **Short TTL**: OTP codes expire after 5 minutes
3. **Rate limiting**: Prevents brute-force attacks
4. **Attempt limiting**: Max 3 verification attempts per OTP
5. **Secure random generation**: Uses `SecureRandom` for all code generation
6. **HTTPS required**: Production deployment must use TLS
7. **JWT authentication**: All endpoints require valid JWT token

## Known Limitations

1. **Notification Service integration**: Currently logs OTP codes instead of sending via SMS/Email (requires Notification Service)
2. **No SMS provider**: SMS delivery requires external provider integration
3. **No email templates**: Email OTP requires template service
4. **Single Redis instance**: No Redis cluster support yet (planned for Phase 13)

## Planned Improvements

1. **Notification Service integration**: Complete SMS/Email delivery
2. **WebAuthn support**: Add FIDO2/WebAuthn as MFA method
3. **Push notifications**: Mobile app push-based MFA
4. **Risk-based MFA**: Integration with Risk-Based Authentication Service
5. **Admin dashboard**: MFA enrollment statistics and monitoring
6. **Biometric MFA**: Fingerprint/Face ID support

## Testing

### Run Unit Tests
```bash
mvn test -pl otp-service
```

### Run Integration Tests
```bash
mvn verify -pl otp-service
```

### Run All Tests
```bash
mvn clean verify -pl otp-service
```

## Deployment

### Docker Build
```bash
docker build -t otp-service:latest -f otp-service/Dockerfile .
```

### Docker Run
```bash
docker run -d \
  --name otp-service \
  -p 8082:8082 \
  -e DB_HOST=postgres \
  -e REDIS_HOST=redis \
  -e KAFKA_BOOTSTRAP_SERVERS=kafka:9092 \
  otp-service:latest
```

### OpenShift Deployment
See `infrastructure/openshift/otp-service-*.yml` for Kubernetes/OpenShift manifests.

## Monitoring

### Health Check
```bash
curl http://localhost:8082/api/actuator/health
```

### Metrics
```bash
curl http://localhost:8082/api/actuator/metrics
```

### Prometheus Metrics
```bash
curl http://localhost:8082/api/actuator/prometheus
```

## Troubleshooting

### OTP Not Received
- Check Notification Service logs
- Verify phone number/email format
- Check rate limiting status

### TOTP Code Invalid
- Verify device time is synchronized
- Check time step tolerance configuration
- Ensure secret was correctly entered in authenticator app

### Redis Connection Failed
- Verify Redis is running: `docker ps | grep redis`
- Check Redis host/port configuration
- Test Redis connection: `redis-cli ping`

### Database Migration Failed
- Check Liquibase logs
- Verify database credentials
- Ensure database exists: `psql -U admin -l`

## Support

For issues or questions, contact the platform team or create an issue in the repository.

## License

Proprietary - Banking Platform
