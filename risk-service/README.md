# Risk-Based Authentication Service

Production-grade risk-based authentication service that provides intelligent, adaptive authentication by analyzing user behavior, device characteristics, and contextual signals.

## Overview

The Risk-Based Authentication Service calculates risk scores (0-100) based on multiple factors and enforces adaptive authentication policies. It integrates with the Identity Service and OTP Service to provide seamless yet secure authentication experiences.

**Key Capabilities**:
- Multi-factor risk scoring (device, location, velocity, time, failed attempts)
- Adaptive authentication (step-up MFA for suspicious activity)
- Real-time risk assessment via Kafka events
- Configurable risk rules and thresholds
- Risk history tracking for continuous learning

## Bounded Context

**Domain**: Authentication & Security  
**Responsibility**: Risk assessment and adaptive authentication enforcement  
**Dependencies**: Identity Service, OTP & MFA Service, Device Intelligence Service (Feature 5)

## Risk Scoring Algorithm

### Risk Factors (Weighted)
1. **New Device** (25%): First time seeing this device fingerprint
2. **New Location** (20%): Login from unusual geographic location  
3. **Velocity** (15%): Multiple logins in short time period
4. **Time of Day** (10%): Login at unusual hours for this user
5. **Failed Attempts** (30%): Recent failed login attempts

### Risk Levels & Actions
- **Low (0-30)**: Allow authentication, no additional steps
- **Medium (31-60)**: Require MFA (trigger OTP Service)
- **High (61-100)**: Block authentication, alert security team

## Prerequisites

- Java 25 (or Java 21 LTS for immediate compilation)
- Maven 3.9+
- Docker & Docker Compose
- PostgreSQL 16
- Redis 7
- Apache Kafka 3.x

## Local Setup

### 1. Environment Configuration

```bash
cp .env.example .env
# Edit .env with your configuration
```

### 2. Start Infrastructure

```bash
docker-compose up -d postgres redis kafka
```

### 3. Create Database

```bash
docker exec -it banking-postgres psql -U admin -c "CREATE DATABASE risk_db;"
```

### 4. Run the Service

```bash
mvn spring-boot:run -pl risk-service
```

Service starts on `http://localhost:8083/api`

## API Endpoints

### Risk Assessment

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/v1/risk/assess` | Required | Assess authentication risk |
| GET | `/v1/risk/assessment/{id}` | Required | Get assessment details |
| GET | `/v1/risk/history/{userId}` | Required | Get user risk history |

### Risk Rules Management

| Method | Path | Auth | Role | Description |
|--------|------|------|------|-------------|
| GET | `/v1/risk/rules` | Required | ADMIN | List all risk rules |
| POST | `/v1/risk/rules` | Required | ADMIN | Create new risk rule |
| PUT | `/v1/risk/rules/{id}` | Required | ADMIN | Update risk rule |
| DELETE | `/v1/risk/rules/{id}` | Required | ADMIN | Delete risk rule |

## Sample Requests

### 1. Assess Risk

```bash
curl -X POST http://localhost:8083/api/v1/risk/assess \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "sessionId": "abc-123-def-456",
    "deviceFingerprint": "chrome-windows-192.168.1.1",
    "ipAddress": "192.168.1.1",
    "userAgent": "Mozilla/5.0...",
    "geolocation": {
      "country": "US",
      "city": "New York",
      "latitude": 40.7128,
      "longitude": -74.0060
    }
  }'
```

**Response:**
```json
{
  "success": true,
  "data": {
    "assessmentId": "assessment-uuid",
    "riskScore": 45,
    "riskLevel": "MEDIUM",
    "riskAction": "REQUIRE_MFA",
    "factors": {
      "newDevice": 25,
      "newLocation": 0,
      "velocity": 10,
      "timeOfDay": 5,
      "failedAttempts": 5
    },
    "mfaRequired": true,
    "assessedAt": "2024-01-01T00:00:00Z"
  },
  "error": null,
  "traceId": "trace-123",
  "timestamp": "2024-01-01T00:00:00Z"
}
```

### 2. Get Risk History

```bash
curl -X GET http://localhost:8083/api/v1/risk/history/550e8400-e29b-41d4-a716-446655440000 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Environment Variables

| Variable | Default | Description | Required |
|----------|---------|-------------|----------|
| `SERVER_PORT` | 8083 | HTTP server port | No |
| `DB_HOST` | localhost | PostgreSQL host | Yes |
| `DB_NAME` | risk_db | Database name | Yes |
| `REDIS_HOST` | localhost | Redis host | Yes |
| `KAFKA_BOOTSTRAP_SERVERS` | localhost:9092 | Kafka brokers | Yes |
| `RISK_THRESHOLD_LOW` | 30 | Low risk threshold | No |
| `RISK_THRESHOLD_MEDIUM` | 60 | Medium risk threshold | No |
| `RISK_WEIGHT_NEW_DEVICE` | 25 | New device weight (%) | No |
| `RISK_WEIGHT_NEW_LOCATION` | 20 | New location weight (%) | No |
| `RISK_WEIGHT_VELOCITY` | 15 | Velocity weight (%) | No |
| `RISK_WEIGHT_TIME_OF_DAY` | 10 | Time of day weight (%) | No |
| `RISK_WEIGHT_FAILED_ATTEMPTS` | 30 | Failed attempts weight (%) | No |

## Architecture Decisions

### 1. Multi-Factor Risk Scoring
Combines multiple signals to provide holistic risk assessment rather than relying on single factor.

### 2. Configurable Weights
Risk factor weights are configurable to allow tuning based on observed fraud patterns.

### 3. Redis Caching
Risk scores cached for 5 minutes to reduce database load for repeated assessments within short time windows.

### 4. Event-Driven Integration
Consumes login events from Identity Service via Kafka for real-time risk assessment without tight coupling.

### 5. Adaptive Actions
Risk actions (Allow, Require MFA, Block) automatically enforced based on calculated risk score.

## Kafka Integration

### Topics Consumed
- `banking.identity.login-attempted`: Triggers risk assessment on login attempt
- `banking.identity.login-succeeded`: Updates risk history on successful login
- `banking.transactions.created`: Monitors transaction patterns for velocity checks

### Topics Produced
- `banking.risk.assessment-completed`: Risk assessment results for audit trail
- `banking.risk.high-risk-detected`: High-risk alerts for security monitoring
- `banking.risk.mfa-required`: Triggers MFA flow in OTP Service

## Security Considerations

1. **No PII in Risk Scores**: Risk scores are numeric only, no sensitive data exposed
2. **Encrypted Device Fingerprints**: Device fingerprints hashed before storage
3. **Audit Trail**: All risk assessments logged immutably
4. **Rate Limiting**: Prevents abuse of risk assessment API
5. **Admin-Only Rule Management**: Only admins can modify risk rules

## Known Limitations

1. **Device Intelligence Integration**: Requires Feature 5 (Device Intelligence Service) for advanced device fingerprinting
2. **Machine Learning**: Current implementation uses rule-based scoring; ML models planned for Phase 5
3. **Geolocation Accuracy**: Depends on IP geolocation database accuracy
4. **Historical Data**: Requires time to build user behavior baselines

## Planned Improvements

1. **ML-Based Scoring**: Train models on historical risk data
2. **Behavioral Biometrics**: Typing patterns, mouse movements
3. **Network Analysis**: Detect botnet patterns
4. **Threat Intelligence**: Integration with external threat feeds
5. **User Risk Profiles**: Per-user baseline risk profiles

## Monitoring

### Health Check
```bash
curl http://localhost:8083/api/actuator/health
```

### Metrics
```bash
curl http://localhost:8083/api/actuator/metrics
```

### Custom Metrics
- `banking.risk.assessments.total` (tags: risk_level, action)
- `banking.risk.score.distribution` (histogram)
- `banking.risk.mfa.triggered.total`
- `banking.risk.blocked.total`

## Troubleshooting

### High Risk Scores for Legitimate Users
- Check risk factor weights configuration
- Review user's recent login history
- Verify geolocation database accuracy

### MFA Not Triggered
- Verify OTP Service integration
- Check Kafka topic connectivity
- Review risk threshold configuration

### Performance Issues
- Check Redis cache hit rate
- Review database query performance
- Monitor Kafka consumer lag

## Support

For issues or questions, contact the platform team or create an issue in the repository.

## License

Proprietary - Banking Platform
