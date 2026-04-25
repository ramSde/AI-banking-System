# Fraud Detection Service

Real-time fraud detection and prevention service for the banking platform.

## Overview

The Fraud Detection Service provides comprehensive fraud detection capabilities using rule-based analysis and real-time transaction monitoring. It analyzes transactions against configurable fraud rules, calculates risk scores, and automatically blocks high-risk transactions.

## Features

- **Rule-Based Detection**: Configurable fraud detection rules with JSON-based configuration
- **Real-Time Analysis**: Immediate fraud checks on transaction events via Kafka
- **Risk Scoring**: Aggregate risk scores (0-100) with LOW/MEDIUM/HIGH classification
- **Auto-Blocking**: Automatic transaction blocking for high-risk scores (>85)
- **Velocity Checks**: Transaction frequency monitoring per user
- **Amount Thresholds**: Large and suspicious amount detection
- **Geographic Anomalies**: Unusual location detection
- **Time Pattern Analysis**: Unusual transaction time detection
- **Alert Management**: Fraud alert creation, assignment, and resolution workflow
- **Pattern Detection**: Behavioral pattern tracking and analysis
- **Redis Caching**: Risk score caching for performance
- **Kafka Integration**: Event-driven architecture for scalability

## Technology Stack

- **Java**: 25
- **Spring Boot**: 3.2.5
- **Spring Security**: JWT-based authentication
- **Spring Data JPA**: PostgreSQL persistence
- **Spring Kafka**: Event streaming
- **Redis**: Caching and session management
- **Liquibase**: Database migrations
- **MapStruct**: DTO mapping
- **Micrometer**: Metrics and observability
- **OpenTelemetry**: Distributed tracing

## Prerequisites

- Java JDK 25
- Maven 3.9+
- PostgreSQL 16+
- Redis 7+
- Apache Kafka 3.5+
- Docker (for containerized deployment)

## Local Setup

### 1. Environment Configuration

Copy the example environment file and configure:

```bash
cp .env.example .env
```

Edit `.env` with your local configuration:

```properties
SPRING_PROFILES_ACTIVE=dev
SERVER_PORT=8088
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/banking_fraud
SPRING_DATASOURCE_USERNAME=admin
SPRING_DATASOURCE_PASSWORD=admin
SPRING_DATA_REDIS_HOST=localhost
SPRING_DATA_REDIS_PORT=6379
SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092
JWT_SECRET=your-secret-key-here
```

### 2. Database Setup

Create the PostgreSQL database:

```sql
CREATE DATABASE banking_fraud;
```

Liquibase will automatically run migrations on startup.

### 3. Start Dependencies

Using Docker Compose:

```bash
docker-compose up -d postgres redis kafka
```

### 4. Build and Run

```bash
# Build
mvn clean package

# Run
java -jar target/fraud-detection-service-1.0.0.jar
```

Or using Maven:

```bash
mvn spring-boot:run
```

The service will start on `http://localhost:8088`

## API Endpoints

### Fraud Rules Management (Admin Only)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/fraud/rules` | Create fraud rule |
| GET | `/api/v1/fraud/rules` | List all rules |
| GET | `/api/v1/fraud/rules/{id}` | Get rule by ID |
| PUT | `/api/v1/fraud/rules/{id}` | Update rule |
| DELETE | `/api/v1/fraud/rules/{id}` | Delete rule |
| PATCH | `/api/v1/fraud/rules/{id}/status` | Toggle rule status |

### Fraud Alerts Management (Admin/Support)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/fraud/alerts` | List all alerts |
| GET | `/api/v1/fraud/alerts/{id}` | Get alert by ID |
| GET | `/api/v1/fraud/alerts/open` | Get open alerts |
| GET | `/api/v1/fraud/alerts/status/{status}` | Get alerts by status |
| PATCH | `/api/v1/fraud/alerts/{id}/assign` | Assign alert |
| PATCH | `/api/v1/fraud/alerts/{id}/status` | Update alert status |

### Fraud Detection (Internal/System)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/fraud/check` | Perform fraud check |
| GET | `/api/v1/fraud/score/{transactionId}` | Get risk score |

### Health Check

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/fraud/health` | Service health check |
| GET | `/actuator/health` | Actuator health endpoint |
| GET | `/actuator/prometheus` | Prometheus metrics |

## Authentication

All endpoints (except health checks) require JWT authentication:

```bash
curl -H "Authorization: Bearer <JWT_TOKEN>" \
     http://localhost:8088/api/v1/fraud/rules
```

## Kafka Topics

### Consumed Topics

- `banking.transaction.transaction-created`: Real-time transaction fraud checks
- `banking.transaction.transaction-completed`: Post-transaction analysis

### Produced Topics

- `banking.fraud.alert-raised`: Fraud alert notifications
- `banking.fraud.transaction-blocked`: Transaction blocking events
- `banking.fraud.pattern-detected`: Pattern detection events

## Fraud Rules

### Default Rules

The service includes 7 default fraud detection rules:

1. **High Velocity Transaction Check**: Detects high transaction frequency
2. **Large Amount Transaction**: Flags large transactions (>$10,000)
3. **Suspicious Amount Pattern**: Detects very large amounts (>$50,000)
4. **Geographic Anomaly Detection**: Unusual location detection
5. **Unusual Time Pattern**: Transactions during unusual hours (2-5 AM)
6. **New Account Risk**: Higher risk for new accounts (<7 days)
7. **Multiple Failed Attempts**: Detects repeated failed attempts

### Rule Configuration

Rules are configured with JSON:

```json
{
  "ruleName": "High Velocity Check",
  "ruleType": "VELOCITY",
  "description": "Detects high transaction frequency",
  "ruleConfig": {
    "maxTransactions": 10,
    "windowMinutes": 60,
    "scoreContribution": 25
  },
  "weight": 25,
  "enabled": true
}
```

## Risk Scoring

- **Score Range**: 0-100
- **LOW Risk**: 0-30 (Allow)
- **MEDIUM Risk**: 31-70 (Flag for monitoring)
- **HIGH Risk**: 71-100 (Require verification)
- **Auto-Block**: Score ≥ 85

## Environment Variables

| Variable | Default | Description | Required |
|----------|---------|-------------|----------|
| `SPRING_PROFILES_ACTIVE` | dev | Active profile (dev/staging/prod) | Yes |
| `SERVER_PORT` | 8088 | Server port | Yes |
| `SPRING_DATASOURCE_URL` | - | PostgreSQL JDBC URL | Yes |
| `SPRING_DATASOURCE_USERNAME` | - | Database username | Yes |
| `SPRING_DATASOURCE_PASSWORD` | - | Database password | Yes |
| `SPRING_DATA_REDIS_HOST` | localhost | Redis host | Yes |
| `SPRING_DATA_REDIS_PORT` | 6379 | Redis port | Yes |
| `SPRING_DATA_REDIS_PASSWORD` | - | Redis password | No |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | - | Kafka bootstrap servers | Yes |
| `JWT_SECRET` | - | JWT signing secret | Yes |
| `FRAUD_HIGH_RISK_THRESHOLD` | 70 | High risk threshold | No |
| `FRAUD_MEDIUM_RISK_THRESHOLD` | 30 | Medium risk threshold | No |
| `FRAUD_AUTO_BLOCK_THRESHOLD` | 85 | Auto-block threshold | No |
| `FRAUD_VELOCITY_WINDOW` | 60 | Velocity window (minutes) | No |
| `FRAUD_MAX_TRANSACTIONS` | 10 | Max transactions per window | No |
| `FRAUD_LARGE_AMOUNT` | 10000.00 | Large amount threshold | No |
| `FRAUD_SUSPICIOUS_AMOUNT` | 50000.00 | Suspicious amount threshold | No |

## Docker Deployment

### Build Image

```bash
docker build -t fraud-detection-service:latest .
```

### Run Container

```bash
docker run -d \
  --name fraud-detection-service \
  -p 8088:8088 \
  --env-file .env \
  fraud-detection-service:latest
```

## Kubernetes/OpenShift Deployment

### Apply Manifests

```bash
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/hpa.yaml
```

### Verify Deployment

```bash
kubectl get pods -l app=fraud-detection-service
kubectl logs -f deployment/fraud-detection-service
```

## Monitoring

### Metrics

Prometheus metrics available at `/actuator/prometheus`:

- `banking.fraud.checks.total`: Total fraud checks performed
- `banking.fraud.alerts.raised`: Total fraud alerts raised
- `banking.fraud.rules.executed`: Total rule executions
- `banking.fraud.score.distribution`: Risk score distribution

### Health Checks

- **Liveness**: `/actuator/health/liveness`
- **Readiness**: `/actuator/health/readiness`

### Logging

Structured JSON logs with fields:
- `timestamp`: ISO 8601 UTC timestamp
- `level`: Log level (INFO, DEBUG, ERROR)
- `service`: fraud-detection-service
- `traceId`: Distributed trace ID
- `message`: Log message

## Architecture Decisions

### Rule Engine Design

- **JSON Configuration**: Flexible rule configuration without code changes
- **Weight-Based Scoring**: Weighted contribution to aggregate risk score
- **Hot Reload**: Rules can be enabled/disabled without restart

### Caching Strategy

- **Redis**: Risk scores cached for 24 hours
- **Cache Key**: `fraud:risk-score:{transactionId}`
- **TTL**: Configurable per environment

### Event-Driven Architecture

- **Kafka**: Asynchronous transaction processing
- **Manual Commit**: Ensures at-least-once processing
- **DLQ**: Dead letter queue for failed events

## Known Limitations

1. **Geographic Detection**: Requires location metadata in transaction events
2. **Account Age**: Requires account creation date in metadata
3. **ML Integration**: ML-based scoring not yet implemented (planned)
4. **Pattern Learning**: Manual pattern configuration (auto-learning planned)

## Future Improvements

1. Machine learning model integration for behavioral scoring
2. Real-time pattern learning and adaptation
3. Advanced geographic anomaly detection with IP geolocation
4. Device fingerprinting integration
5. Biometric authentication support
6. Enhanced reporting and analytics dashboard

## Support

For issues or questions, contact the platform team or create an issue in the repository.

## License

Proprietary - Banking Platform
