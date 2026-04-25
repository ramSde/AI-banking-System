# User Service

## Overview

The User Service is a production-grade microservice responsible for user profile management, user preferences, and KYC (Know Your Customer) verification status tracking. It provides secure storage of PII (Personally Identifiable Information) with AES-256-GCM encryption at rest and masked display in API responses.

**Bounded Context**: User Management & Profile

## Purpose

- Manage user profiles with encrypted PII fields (phone, name, DOB, address)
- Track user preferences (language, timezone, notifications, security settings)
- Manage KYC document uploads and verification status
- Provide user data for authentication and authorization flows
- Maintain audit trail of user account changes

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

Edit `.env` with your local configuration. **IMPORTANT**: Change the encryption key to a secure 32-byte value.

### 2. Start Infrastructure

```bash
docker-compose up -d postgres redis kafka
```

### 3. Build Application

```bash
mvn clean install
```

### 4. Run Application

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

The service will start on `http://localhost:8085/api`

## API Endpoints

| Method | Path | Auth Required | Role | Description |
|--------|------|---------------|------|-------------|
| POST | `/v1/users` | Yes | USER, ADMIN | Create user profile |
| GET | `/v1/users/me` | Yes | USER, ADMIN | Get own profile |
| PUT | `/v1/users/me` | Yes | USER, ADMIN | Update own profile |
| DELETE | `/v1/users/me` | Yes | USER, ADMIN | Delete own profile |
| GET | `/v1/users/{id}` | Yes | ADMIN | Get user by ID |
| GET | `/v1/users` | Yes | ADMIN | List all users |
| PUT | `/v1/users/{id}/status` | Yes | ADMIN | Update user status |
| PUT | `/v1/users/{id}/kyc` | Yes | ADMIN | Update KYC status |
| GET | `/v1/preferences/me` | Yes | USER, ADMIN | Get own preferences |
| PUT | `/v1/preferences/me` | Yes | USER, ADMIN | Update own preferences |
| POST | `/v1/kyc/upload` | Yes | USER, ADMIN | Upload KYC document |
| GET | `/v1/kyc/my-documents` | Yes | USER, ADMIN | Get own KYC documents |
| GET | `/v1/kyc/{id}` | Yes | ADMIN | Get KYC document by ID |
| PUT | `/v1/kyc/{id}/verify` | Yes | ADMIN | Verify KYC document |
| PUT | `/v1/kyc/{id}/reject` | Yes | ADMIN | Reject KYC document |

## Environment Variables

| Variable | Default | Description | Required |
|----------|---------|-------------|----------|
| SERVER_PORT | 8085 | Server port | No |
| DB_URL | jdbc:postgresql://localhost:5432/user_db | Database URL | Yes |
| DB_USERNAME | admin | Database username | Yes |
| DB_PASSWORD | admin | Database password | Yes |
| REDIS_HOST | localhost | Redis host | Yes |
| REDIS_PORT | 6379 | Redis port | Yes |
| KAFKA_BOOTSTRAP_SERVERS | localhost:9092 | Kafka servers | Yes |
| USER_ENCRYPTION_KEY | (32 bytes) | AES-256 encryption key | Yes |
| USER_CACHE_PROFILE_TTL | 15 | Profile cache TTL (minutes) | No |
| USER_CACHE_PREFERENCE_TTL | 30 | Preference cache TTL (minutes) | No |
| USER_KYC_DOC_MAX_SIZE | 10 | Max KYC document size (MB) | No |
| USER_KYC_EXPIRY_DAYS | 365 | KYC verification expiry (days) | No |
| JWT_SECRET | (256 bits) | JWT secret key | Yes |

## Architecture Decisions

### 1. PII Encryption
- **Decision**: AES-256-GCM encryption for all PII fields
- **Rationale**: Regulatory compliance (GDPR, PCI-DSS) requires encryption at rest
- **Tradeoff**: Slight performance overhead, cannot query encrypted fields directly

### 2. Dual Storage (Encrypted + Masked)
- **Decision**: Store both encrypted and masked versions of PII
- **Rationale**: Fast display without decryption, audit trail preservation
- **Tradeoff**: Additional storage space (minimal impact)

### 3. Redis Caching
- **Decision**: 15-minute TTL for profiles, 30-minute for preferences
- **Rationale**: Balance between performance and data freshness
- **Tradeoff**: Potential stale data for TTL duration

### 4. Soft Delete
- **Decision**: Soft delete with deleted_at timestamp
- **Rationale**: GDPR right to be forgotten, audit trail, recovery capability
- **Tradeoff**: Requires periodic cleanup job

### 5. KYC Document Storage
- **Decision**: Store metadata in PostgreSQL, files in object storage (S3/MinIO)
- **Rationale**: PostgreSQL not optimized for binary data, scalability
- **Tradeoff**: Additional infrastructure dependency

## Known Limitations

1. **Encryption Key Rotation**: Manual process, requires data re-encryption
2. **Search on Encrypted Fields**: Cannot perform full-text search on encrypted PII
3. **Cache Invalidation**: TTL-based only, no event-driven invalidation
4. **File Upload**: Synchronous upload, may timeout for large files

## Planned Improvements

1. Automated encryption key rotation with versioning
2. Async file upload with progress tracking
3. Event-driven cache invalidation via Kafka
4. Advanced search with encrypted field indexing (searchable encryption)
5. Biometric data storage for enhanced authentication

## Kafka Topics

### Consumed
- `banking.identity.user-registered` - User registration events from Identity Service

### Produced
- `banking.user.user-created` - User profile created
- `banking.user.user-updated` - User profile updated
- `banking.user.user-deleted` - User profile deleted
- `banking.user.kyc-status-changed` - KYC status changed
- `banking.user.preference-updated` - User preferences updated

## Database Schema

### Tables
- `user` - User profiles with encrypted PII
- `user_preference` - User preferences and settings
- `kyc_document` - KYC verification documents metadata

## Monitoring

### Health Checks
- Liveness: `GET /api/actuator/health/liveness`
- Readiness: `GET /api/actuator/health/readiness`

### Metrics
- Prometheus: `GET /api/actuator/prometheus`
- Custom metrics:
  - `banking.user.created.total`
  - `banking.user.kyc.verified.total`
  - `banking.user.encryption.latency`

## Security

- JWT RS256 authentication required for all endpoints
- Role-based access control (RBAC)
- PII encryption at rest (AES-256-GCM)
- PII masking in API responses
- Rate limiting via Redis sliding window
- Audit logging for all PII access
- No sensitive data in logs

## Support

For issues or questions, contact the platform team or create an issue in the repository.
