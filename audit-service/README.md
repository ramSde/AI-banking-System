# Audit Service

## Overview

The Audit Service is a production-grade, immutable audit trail system for the banking platform. It provides comprehensive tracking of all banking operations, ensuring compliance with regulatory requirements and enabling forensic investigation of security incidents. The service consumes audit events from Kafka topics across all microservices and stores them in an immutable PostgreSQL database with a 7-year retention policy.

**Bounded Context**: Audit & Compliance Domain

The Audit Service is responsible for:
- Consuming audit events from all banking services via Kafka
- Storing immutable audit records (NO UPDATE or DELETE operations)
- Calculating JSON diffs between before/after states
- Providing query APIs for audit trail investigation
- Supporting compliance with regulatory requirements (7-year retention)
- Enabling forensic analysis of security incidents

## Why It Exists

**Business Justification**:
- **Regulatory Compliance**: Banking regulations require comprehensive audit trails for all financial operations
- **Security Investigation**: Enables forensic analysis of security incidents and fraud detection
- **Accountability**: Provides complete visibility into who did what, when, and from where
- **Dispute Resolution**: Supports investigation and resolution of customer disputes
- **Operational Transparency**: Enables monitoring and analysis of system behavior

**What Breaks Without This Service**:
- Inability to meet regulatory compliance requirements
- No forensic capability for security incident investigation
- Lack of accountability for system operations
- Inability to resolve customer disputes with evidence
- No audit trail for compliance audits

## Dependencies

### Upstream Services
This service depends on:
- **All Banking Services**: Consumes audit events from `banking.*.audit-event` Kafka topics
- **Identity Service**: For JWT token validation and user authentication

### Infrastructure Dependencies
- **PostgreSQL**: Database `audit_db` for storing immutable audit events
- **Kafka**: Consumes from topic pattern `banking.*.audit-event` (wildcard for all services)
- **Redis**: Not required (audit service is stateless)

## What It Unlocks

This service enables:
- **Feature 11**: Notification Service (can query audit events for notification triggers)
- **Feature 22**: Admin Dashboard API (provides audit trail data for admin dashboards)
- **Feature 30**: Admin/Backoffice Service (enables manual investigation and dispute resolution)
- **Compliance Reporting**: Enables generation of compliance reports for auditors
- **Security Analytics**: Enables analysis of security patterns and anomaly detection

## Prerequisites

- Java 17 or higher
- Maven 3.9+
- Docker and Docker Compose
- PostgreSQL 16+
- Apache Kafka 3.5+
- Redis 7+ (optional, for rate limiting)

## Local Setup

### 1. Environment Configuration

Copy the example environment file and configure it:

```bash
cp .env.example .env
```

Edit `.env` and set appropriate values for your local environment.

### 2. Start Infrastructure

Start PostgreSQL and Kafka using Docker Compose:

```bash
docker-compose up -d postgres kafka zookeeper
```

### 3. Build the Application

```bash
mvn clean package -DskipTests
```

### 4. Run the Application

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Or run the JAR directly:

```bash
java -jar target/audit-service-1.0.0.jar --spring.profiles.active=dev
```

The service will be available at: `http://localhost:8090/api`

### 5. Access Swagger UI

Open your browser and navigate to:
```
http://localhost:8090/api/swagger-ui.html
```

## API Summary

| Method | Path | Auth Required | Role | Description |
|--------|------|---------------|------|-------------|
| POST | `/v1/audit/events/query` | Yes | USER, ADMIN, SUPPORT | Query audit events with flexible filtering |
| GET | `/v1/audit/events/{id}` | Yes | USER, ADMIN, SUPPORT | Get audit event by ID |
| GET | `/v1/audit/events/event-id/{eventId}` | Yes | USER, ADMIN, SUPPORT | Get audit event by event ID |
| GET | `/v1/audit/events/entity/{entityType}/{entityId}` | Yes | USER, ADMIN, SUPPORT | Get audit events by entity |
| GET | `/v1/audit/events/user/{userId}` | Yes | USER, ADMIN, SUPPORT | Get audit events by user |
| GET | `/v1/audit/events/type/{eventType}` | Yes | ADMIN, SUPPORT | Get audit events by event type |
| GET | `/v1/audit/trace/{traceId}` | Yes | ADMIN, SUPPORT | Get audit events by trace ID |
| GET | `/v1/audit/correlation/{correlationId}` | Yes | ADMIN, SUPPORT | Get audit events by correlation ID |
| GET | `/v1/audit/admin/count/entity/{entityType}/{entityId}` | Yes | ADMIN | Count audit events by entity |
| GET | `/v1/audit/admin/count/user/{userId}` | Yes | ADMIN | Count audit events by user and date range |

## Environment Variables Reference

| Variable | Default | Description | Required |
|----------|---------|-------------|----------|
| SPRING_PROFILES_ACTIVE | dev | Active Spring profile (dev, staging, prod) | Yes |
| DB_HOST | localhost | PostgreSQL database host | Yes |
| DB_PORT | 5432 | PostgreSQL database port | Yes |
| DB_NAME | audit_db | PostgreSQL database name | Yes |
| DB_USERNAME | admin | PostgreSQL database username | Yes |
| DB_PASSWORD | admin | PostgreSQL database password | Yes |
| DB_SSL | false | Enable SSL for database connection | No |
| KAFKA_BOOTSTRAP_SERVERS | localhost:9092 | Kafka bootstrap servers | Yes |
| KAFKA_CONSUMER_GROUP | banking-audit-service-consumer-group | Kafka consumer group ID | Yes |
| KAFKA_AUTO_OFFSET_RESET | earliest | Kafka auto offset reset strategy | No |
| JWT_SECRET | (see .env.example) | JWT secret key (minimum 256 bits) | Yes |
| JWT_EXPIRATION | 900000 | JWT expiration time in milliseconds | No |
| AUDIT_RETENTION_DAYS | 2555 | Audit event retention period (7 years) | No |
| AUDIT_QUERY_MAX_RESULTS | 1000 | Maximum results per query | No |
| AUDIT_PARTITION_ENABLED | true | Enable table partitioning | No |
| SERVER_PORT | 8090 | Server port | No |

## Running Tests

### Unit Tests Only
```bash
mvn test
```

### Integration Tests Only
```bash
mvn verify -DskipUnitTests
```

### All Tests
```bash
mvn verify
```

### With Coverage Report
```bash
mvn verify jacoco:report
```

Coverage report will be available at: `target/site/jacoco/index.html`

## Architecture Decisions

### 1. Immutable Audit Trail
**Decision**: Audit events are immutable - NO UPDATE or DELETE operations allowed.

**Reasoning**:
- Ensures integrity of audit trail for compliance
- Prevents tampering with historical records
- Simplifies data model and query patterns

**Implementation**: Database trigger prevents UPDATE/DELETE operations on `audit_events` table.

### 2. Kafka-Based Event Consumption
**Decision**: Consume audit events from Kafka using wildcard topic pattern `banking.*.audit-event`.

**Reasoning**:
- Decouples audit service from source services
- Enables asynchronous, non-blocking audit logging
- Provides natural buffering and replay capability

**Tradeoff**: Eventual consistency - audit events may lag behind source operations.

### 3. JSON Diff Calculation
**Decision**: Calculate JSON diffs between before/after states using zjsonpatch library.

**Reasoning**:
- Provides precise change tracking
- Reduces storage requirements
- Enables efficient change analysis

**Tradeoff**: Additional CPU overhead during event processing.

### 4. Table Partitioning (Optional)
**Decision**: Support quarterly table partitioning for large-scale deployments.

**Reasoning**:
- Improves query performance on large datasets
- Enables efficient data archival and retention management
- Reduces index size and maintenance overhead

**Tradeoff**: Increased operational complexity.

### 5. Comprehensive Indexing
**Decision**: Create 18 indexes covering all common query patterns.

**Reasoning**:
- Optimizes read performance for audit queries
- Supports efficient filtering by entity, user, time, trace ID, etc.
- Enables fast forensic investigation

**Tradeoff**: Increased write latency and storage overhead.

## Known Limitations

1. **Eventual Consistency**: Audit events may lag behind source operations due to Kafka processing.

2. **No Real-Time Alerts**: This service stores audit events but does not generate real-time alerts. Use Fraud Detection Service for real-time alerting.

3. **Storage Growth**: Audit events accumulate over 7 years. Plan for significant storage requirements.

4. **Query Performance**: Large date ranges may result in slow queries. Use pagination and specific filters.

5. **No Built-in Archival**: Retention policy enforcement requires manual or scheduled cleanup jobs.

## Planned Improvements

1. **Automated Archival**: Implement scheduled job to archive old audit events to cold storage.

2. **Advanced Search**: Add full-text search capability using PostgreSQL tsvector or Elasticsearch.

3. **Real-Time Streaming**: Expose audit event stream via WebSocket for real-time monitoring.

4. **Compliance Reports**: Add pre-built compliance report generation (SOX, PCI-DSS, GDPR).

5. **Anomaly Detection**: Integrate ML-based anomaly detection on audit patterns.

6. **Data Encryption**: Add field-level encryption for sensitive audit data.

## Kafka Topics

### Consumed Topics
- **Pattern**: `banking.*.audit-event`
- **Examples**:
  - `banking.account.audit-event`
  - `banking.transaction.audit-event`
  - `banking.user.audit-event`
  - `banking.fraud.audit-event`

### Event Schema
```json
{
  "eventId": "uuid",
  "eventType": "ACCOUNT_CREATED",
  "entityType": "ACCOUNT",
  "entityId": "uuid",
  "actorUserId": "uuid",
  "actorUsername": "john.doe",
  "actorIp": "192.168.1.100",
  "actorDeviceId": "device-fingerprint-id",
  "actorUserAgent": "Mozilla/5.0...",
  "beforeState": { ... },
  "afterState": { ... },
  "occurredAt": "2024-01-01T00:00:00Z",
  "traceId": "trace-id",
  "spanId": "span-id",
  "correlationId": "correlation-id",
  "sessionId": "session-id",
  "serviceName": "account-service",
  "action": "CREATE",
  "status": "SUCCESS",
  "errorMessage": null,
  "metadata": { ... }
}
```

## Database Schema

### audit_events Table
- **id**: UUID (Primary Key)
- **event_id**: VARCHAR(255) (Unique)
- **event_type**: VARCHAR(100) (Enum)
- **entity_type**: VARCHAR(100) (Enum)
- **entity_id**: VARCHAR(255)
- **actor_user_id**: UUID
- **actor_username**: VARCHAR(255)
- **actor_ip**: VARCHAR(45)
- **actor_device_id**: VARCHAR(255)
- **actor_user_agent**: TEXT
- **before_state**: JSONB
- **after_state**: JSONB
- **changes**: JSONB
- **occurred_at**: TIMESTAMPTZ
- **trace_id**: VARCHAR(255)
- **span_id**: VARCHAR(255)
- **correlation_id**: VARCHAR(255)
- **session_id**: VARCHAR(255)
- **service_name**: VARCHAR(100)
- **action**: VARCHAR(100)
- **status**: VARCHAR(50)
- **error_message**: TEXT
- **metadata**: JSONB
- **created_at**: TIMESTAMPTZ
- **updated_at**: TIMESTAMPTZ
- **deleted_at**: TIMESTAMPTZ
- **version**: BIGINT

### Indexes
- idx_audit_events_event_id
- idx_audit_events_entity
- idx_audit_events_actor_user_id
- idx_audit_events_occurred_at
- idx_audit_events_entity_time
- idx_audit_events_user_time
- idx_audit_events_event_type
- idx_audit_events_trace_id
- idx_audit_events_correlation_id
- idx_audit_events_service_name
- idx_audit_events_action
- idx_audit_events_status
- idx_audit_events_service_event_time
- idx_audit_events_before_state_gin (JSONB)
- idx_audit_events_after_state_gin (JSONB)
- idx_audit_events_changes_gin (JSONB)
- idx_audit_events_metadata_gin (JSONB)
- idx_audit_events_created_at

## Deployment

### Docker Build
```bash
docker build -t your-registry/audit-service:latest .
```

### Docker Run
```bash
docker run -d \
  --name audit-service \
  -p 8090:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_HOST=postgres \
  -e DB_PORT=5432 \
  -e DB_NAME=audit_db \
  -e DB_USERNAME=admin \
  -e DB_PASSWORD=secret \
  -e KAFKA_BOOTSTRAP_SERVERS=kafka:9092 \
  -e JWT_SECRET=your-secret-key \
  your-registry/audit-service:latest
```

### Kubernetes Deployment
```bash
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/hpa.yaml
```

### OpenShift Deployment
```bash
oc apply -f k8s/configmap.yaml
oc apply -f k8s/deployment.yaml
oc apply -f k8s/service.yaml
oc apply -f k8s/hpa.yaml
```

## Monitoring

### Health Check
```bash
curl http://localhost:8090/api/actuator/health
```

### Metrics
```bash
curl http://localhost:8090/api/actuator/metrics
```

### Prometheus Metrics
```bash
curl http://localhost:8090/api/actuator/prometheus
```

## Security

- **JWT Authentication**: All endpoints require valid JWT token
- **Role-Based Access Control**: Different endpoints require different roles (USER, ADMIN, SUPPORT)
- **IP Address Tracking**: All audit events capture actor IP address
- **Device Fingerprinting**: All audit events capture device ID
- **Immutable Records**: Database triggers prevent modification of audit events
- **Encrypted Transport**: TLS/HTTPS in production
- **No PII in Logs**: Sensitive data is masked in application logs

## Support

For issues, questions, or contributions, please contact the platform team.

## License

Proprietary - Banking Platform
