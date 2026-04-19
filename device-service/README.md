# Device Intelligence Service

## Overview

The Device Intelligence Service is a production-grade microservice responsible for device fingerprinting, trust scoring, and anomaly detection in the banking platform. It tracks user devices, assigns trust scores based on behavior, and detects suspicious activities to enhance security.

**Bounded Context**: Device Management & Security Intelligence

## Purpose

- Register and track user devices with unique fingerprinting
- Assign and manage trust scores (0-100) based on device behavior
- Detect anomalies such as impossible travel, location changes, and suspicious patterns
- Provide device intelligence for risk-based authentication
- Maintain immutable audit trail of all device events

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

Edit `.env` with your local configuration.

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

The service will start on `http://localhost:8084/api`

## API Endpoints

| Method | Path | Auth Required | Role | Description |
|--------|------|---------------|------|-------------|
| POST | `/v1/devices/register` | Yes | USER, ADMIN | Register new device |
| GET | `/v1/devices/{deviceId}` | Yes | USER, ADMIN | Get device by ID |
| GET | `/v1/devices/user/{userId}` | Yes | USER, ADMIN | Get user's devices |
| GET | `/v1/devices/status/{status}` | Yes | ADMIN | Get devices by status |
| GET | `/v1/devices/suspicious` | Yes | ADMIN | Get suspicious devices |
| GET | `/v1/devices/trusted` | Yes | ADMIN | Get trusted devices |
| PUT | `/v1/devices/{deviceId}/trust` | Yes | ADMIN, SYSTEM | Update trust score |
| POST | `/v1/devices/{deviceId}/block` | Yes | ADMIN | Block device |
| POST | `/v1/devices/{deviceId}/unblock` | Yes | ADMIN | Unblock device |
| DELETE | `/v1/devices/{deviceId}` | Yes | USER, ADMIN | Delete device |
| GET | `/v1/devices/user/{userId}/count` | Yes | USER, ADMIN | Get device count |
| GET | `/v1/devices/inactive` | Yes | ADMIN | Get inactive devices |

## Environment Variables

| Variable | Default | Description | Required |
|----------|---------|-------------|----------|
| SERVER_PORT | 8084 | Server port | No |
| DB_URL | jdbc:postgresql://localhost:5432/device_db | Database URL | Yes |
| DB_USERNAME | admin | Database username | Yes |
| DB_PASSWORD | admin | Database password | Yes |
| REDIS_HOST | localhost | Redis host | Yes |
| REDIS_PORT | 6379 | Redis port | Yes |
| KAFKA_BOOTSTRAP_SERVERS | localhost:9092 | Kafka servers | Yes |
| DEVICE_FINGERPRINT_SALT | banking-device-salt-2024 | Fingerprint salt | Yes |
| DEVICE_TRUST_INITIAL_SCORE | 30 | Initial trust score | No |
| DEVICE_CACHE_TTL_MINUTES | 5 | Cache TTL in minutes | No |
| JWT_SECRET | your-secret | JWT secret key | Yes |

## Running Tests

### Unit Tests Only
```bash
mvn test
```

### Integration Tests Only
```bash
mvn verify -P integration-tests
```

### All Tests
```bash
mvn verify
```

## Architecture Decisions

### 1. Device Fingerprinting
- **Decision**: Use SHA-256 hash of device characteristics
- **Rationale**: Provides unique, deterministic device identification without storing raw data
- **Tradeoff**: Cannot reverse-engineer original device data from hash

### 2. Trust Scoring
- **Decision**: 0-100 scale with dynamic adjustment
- **Rationale**: Simple, intuitive scoring that adapts to device behavior
- **Tradeoff**: Requires tuning of increment/decrement values

### 3. Redis Caching
- **Decision**: 5-minute TTL for device data
- **Rationale**: Balances performance with data freshness
- **Tradeoff**: Potential stale data for 5 minutes

### 4. Soft Delete
- **Decision**: Soft delete with deleted_at timestamp
- **Rationale**: Maintains audit trail and allows recovery
- **Tradeoff**: Requires periodic cleanup of old deleted records

## Known Limitations

1. **Fingerprint Collisions**: Rare but possible with identical device configurations
2. **Trust Score Tuning**: Requires monitoring and adjustment based on real-world data
3. **Geolocation Accuracy**: Depends on IP geolocation service accuracy
4. **Browser Fingerprinting**: Can be bypassed by privacy-focused browsers

## Planned Improvements

1. Machine learning-based anomaly detection
2. Advanced behavioral biometrics (typing patterns, mouse movements)
3. Integration with external threat intelligence feeds
4. Real-time device reputation scoring
5. Enhanced hardware fingerprinting (Canvas, WebGL)

## Kafka Topics

### Consumed
- `banking.identity.login-attempted`
- `banking.identity.login-succeeded`

### Produced
- `banking.device.device-registered`
- `banking.device.trust-changed`
- `banking.device.anomaly-detected`

## Database Schema

### Tables
- `device` - Device registrations with fingerprints and trust scores
- `device_history` - Immutable audit log of device events
- `device_anomaly` - Detected anomalies and security incidents

## Monitoring

### Health Checks
- Liveness: `GET /api/actuator/health/liveness`
- Readiness: `GET /api/actuator/health/readiness`

### Metrics
- Prometheus: `GET /api/actuator/prometheus`
- Custom metrics:
  - `banking.device.registered.total`
  - `banking.device.trust.score.gauge`
  - `banking.device.anomaly.detected.total`

## Security

- JWT RS256 authentication required for all endpoints
- Role-based access control (RBAC)
- PII masking in API responses (IP addresses, fingerprints)
- Rate limiting via Redis sliding window
- Audit logging for all operations

## Support

For issues or questions, contact the platform team or create an issue in the repository.
