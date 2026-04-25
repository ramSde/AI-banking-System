# Notification Service

## Overview

The Notification Service is a multi-channel notification delivery system that supports Email, SMS, and Push notifications. It consumes notification events from Kafka, applies rate limiting and deduplication, renders templates with variable substitution, and dispatches notifications through configured providers (SMTP, Twilio, Firebase FCM).

**Bounded Context:** Communication and notification delivery across all channels with template management, delivery tracking, and comprehensive history.

## Features

- **Multi-Channel Support**: Email (SMTP), SMS (Twilio), Push (Firebase FCM)
- **Template Management**: Create, update, and manage notification templates with variable substitution
- **Rate Limiting**: Per-user, per-channel rate limits enforced via Redis sliding window
- **Deduplication**: Prevent duplicate notifications within configurable time window (default 5 minutes)
- **Retry Logic**: Exponential backoff retry with circuit breaker pattern (3 attempts: 1s → 2s → 4s)
- **Idempotency**: Kafka event-based idempotency using event IDs
- **Notification History**: Complete audit trail of all notifications sent
- **Statistics API**: User-level notification statistics by channel and status
- **Resilience**: Circuit breaker for external providers (Resilience4j)
- **Observability**: Structured JSON logging, Prometheus metrics, distributed tracing

## Prerequisites

- Java 17
- Maven 3.9+
- PostgreSQL 16+
- Redis 7+
- Apache Kafka 3.5+
- Docker (for local development)
- Twilio Account (for SMS)
- Firebase Project (for Push notifications)
- SMTP Server (for Email)

## Local Setup

### 1. Environment Configuration

Copy the example environment file and configure:

```bash
cp .env.example .env
```

Edit `.env` with your actual credentials:
- Database connection details
- Redis connection
- Kafka bootstrap servers
- SMTP credentials
- Twilio credentials (Account SID, Auth Token, From Number)
- Firebase credentials path and project ID

### 2. Database Setup

Create the PostgreSQL database:

```sql
CREATE DATABASE notification_db;
```

Liquibase will automatically run migrations on startup.

### 3. Start Dependencies

Using Docker Compose (from project root):

```bash
docker-compose up -d postgres redis kafka
```

### 4. Build and Run

```bash
mvn clean install
mvn spring-boot:run
```

The service will start on port `8091` (configurable via `SERVER_PORT`).

## API Endpoints

### Notification History

| Method | Path | Auth | Role | Description |
|--------|------|------|------|-------------|
| GET | `/v1/notifications/history` | Yes | USER | Get notification history with filters |
| GET | `/v1/notifications/{id}` | Yes | USER | Get specific notification by ID |
| GET | `/v1/notifications/stats` | Yes | USER | Get notification statistics |

### Template Management

| Method | Path | Auth | Role | Description |
|--------|------|------|------|-------------|
| POST | `/v1/templates` | Yes | ADMIN | Create notification template |
| PUT | `/v1/templates/{id}` | Yes | ADMIN | Update notification template |
| GET | `/v1/templates/{id}` | Yes | USER | Get template by ID |
| GET | `/v1/templates/code/{code}` | Yes | USER | Get template by code |
| GET | `/v1/templates` | Yes | USER | Get all templates (paginated) |
| GET | `/v1/templates/channel/{channel}` | Yes | USER | Get templates by channel |
| DELETE | `/v1/templates/{id}` | Yes | ADMIN | Soft delete template |

### Actuator Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/actuator/health` | Health check |
| GET | `/actuator/health/liveness` | Liveness probe |
| GET | `/actuator/health/readiness` | Readiness probe |
| GET | `/actuator/metrics` | Metrics |
| GET | `/actuator/prometheus` | Prometheus metrics |

## Kafka Integration

### Topics Consumed

- **Pattern**: `banking.*.notification-requested` (wildcard pattern)
- **Consumer Group**: `banking-notification-service-consumer-group`
- **Offset Strategy**: Manual commit (MANUAL_IMMEDIATE)

### Event Schema

```json
{
  "eventId": "uuid",
  "eventType": "NotificationRequested",
  "version": "1.0",
  "occurredAt": "2024-01-01T00:00:00Z",
  "correlationId": "uuid",
  "payload": {
    "userId": "uuid",
    "templateCode": "WELCOME_EMAIL",
    "channel": "EMAIL",
    "recipient": "user@example.com",
    "variables": {
      "userName": "John Doe",
      "verificationLink": "https://..."
    }
  }
}
```

## Environment Variables

| Variable | Default | Description | Required |
|----------|---------|-------------|----------|
| `SPRING_PROFILES_ACTIVE` | dev | Active Spring profile | No |
| `DB_HOST` | localhost | PostgreSQL host | Yes |
| `DB_PORT` | 5432 | PostgreSQL port | Yes |
| `DB_NAME` | notification_db | Database name | Yes |
| `DB_USERNAME` | admin | Database username | Yes |
| `DB_PASSWORD` | admin | Database password | Yes |
| `REDIS_HOST` | localhost | Redis host | Yes |
| `REDIS_PORT` | 6379 | Redis port | Yes |
| `KAFKA_BOOTSTRAP_SERVERS` | localhost:9092 | Kafka servers | Yes |
| `SMTP_HOST` | smtp.gmail.com | SMTP server host | Yes |
| `SMTP_PORT` | 587 | SMTP server port | Yes |
| `SMTP_USERNAME` | - | SMTP username | Yes |
| `SMTP_PASSWORD` | - | SMTP password | Yes |
| `TWILIO_ACCOUNT_SID` | - | Twilio Account SID | Yes (for SMS) |
| `TWILIO_AUTH_TOKEN` | - | Twilio Auth Token | Yes (for SMS) |
| `TWILIO_FROM_NUMBER` | - | Twilio From Number | Yes (for SMS) |
| `FIREBASE_CREDENTIALS_PATH` | - | Firebase credentials JSON path | Yes (for Push) |
| `FIREBASE_PROJECT_ID` | - | Firebase project ID | Yes (for Push) |
| `JWT_SECRET` | - | JWT signing secret (256-bit) | Yes |
| `RATE_LIMIT_EMAIL` | 50 | Email rate limit per user/hour | No |
| `RATE_LIMIT_SMS` | 10 | SMS rate limit per user/hour | No |
| `RATE_LIMIT_PUSH` | 100 | Push rate limit per user/hour | No |
| `DEDUP_WINDOW` | 300 | Deduplication window (seconds) | No |

## Template Variables

Templates support variable substitution using `{{variableName}}` syntax:

```
Subject: Welcome {{userName}}!
Body: Hello {{userName}}, click here to verify: {{verificationLink}}
```

## Rate Limiting

Rate limits are enforced per user per channel per hour:
- **Email**: 50 notifications/hour (configurable)
- **SMS**: 10 notifications/hour (configurable)
- **Push**: 100 notifications/hour (configurable)

Rate limit state is stored in Redis with 1-hour TTL.

## Deduplication

Duplicate notifications are prevented within a configurable window (default 5 minutes). Deduplication key is based on:
- User ID
- Template Code
- Recipient

## Retry Strategy

Failed notifications are retried with exponential backoff:
1. **Attempt 1**: Immediate
2. **Attempt 2**: After 1 second
3. **Attempt 3**: After 2 seconds
4. **Attempt 4**: After 4 seconds

After 3 failed attempts, the notification is marked as FAILED.

## Circuit Breaker

Each notification provider (Email, SMS, Push) has an independent circuit breaker:
- **Failure Rate Threshold**: 50%
- **Wait Duration in Open State**: 30 seconds
- **Permitted Calls in Half-Open State**: 5
- **Sliding Window Size**: 10 calls

## Testing

### Run Unit Tests

```bash
mvn test
```

### Run Integration Tests

```bash
mvn verify
```

### Run All Tests

```bash
mvn clean verify
```

## Docker Build

```bash
docker build -t notification-service:latest .
```

## Kubernetes Deployment

```bash
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/hpa.yaml
```

## Architecture Decisions

### 1. Kafka Consumer Pattern
- **Decision**: Use wildcard topic pattern `banking.*.notification-requested`
- **Rationale**: Allows any service to publish notification events without tight coupling
- **Tradeoff**: Requires consistent event schema across all publishers

### 2. Async Processing
- **Decision**: Process notifications asynchronously using `@Async` with custom thread pool
- **Rationale**: Prevents blocking Kafka consumer threads, improves throughput
- **Tradeoff**: Adds complexity in error handling and observability

### 3. Redis for Rate Limiting
- **Decision**: Use Redis sliding window for rate limiting instead of in-memory
- **Rationale**: Distributed rate limiting across multiple service instances
- **Tradeoff**: Adds Redis as a critical dependency

### 4. Template Rendering
- **Decision**: Simple variable substitution with `{{variable}}` syntax
- **Rationale**: Sufficient for most use cases, avoids complexity of full template engines
- **Tradeoff**: Limited to simple string replacement, no conditionals or loops

### 5. Circuit Breaker per Provider
- **Decision**: Independent circuit breakers for Email, SMS, Push
- **Rationale**: Failure in one channel doesn't affect others
- **Tradeoff**: More configuration and monitoring required

## Known Limitations

1. **Template Rendering**: Only supports simple variable substitution, no conditional logic
2. **Attachment Support**: Email attachments not currently supported
3. **Scheduled Notifications**: No support for delayed/scheduled notifications
4. **Bulk Operations**: No batch notification API (must send individual events)
5. **Provider Fallback**: No automatic fallback to alternative providers on failure

## Planned Improvements

1. Add support for email attachments
2. Implement scheduled/delayed notifications
3. Add bulk notification API
4. Support for rich push notifications (images, actions)
5. Template versioning and A/B testing
6. Provider fallback mechanism
7. Notification preferences management
8. Webhook callbacks for delivery status

## Monitoring

### Key Metrics

- `notification.sent.total` - Total notifications sent by channel
- `notification.failed.total` - Total failed notifications by channel
- `notification.rate_limited.total` - Total rate-limited notifications
- `notification.deduplicated.total` - Total deduplicated notifications
- `notification.delivery.duration` - Notification delivery latency

### Health Checks

- **Liveness**: `/actuator/health/liveness` - Service is running
- **Readiness**: `/actuator/health/readiness` - Service is ready to accept traffic

### Logs

Structured JSON logs include:
- `timestamp` - ISO 8601 UTC timestamp
- `level` - Log level (INFO, WARN, ERROR)
- `service` - Service name (notification-service)
- `traceId` - Distributed trace ID
- `userId` - User ID (if authenticated)
- `message` - Log message

## Support

For issues or questions, contact the platform team or create an issue in the project repository.
