# AI Insight Service

## Overview

The AI Insight Service provides personalized financial intelligence by analyzing user transaction patterns, spending behavior, and account activity to generate actionable insights and recommendations.

## Features

- **Spending Pattern Analysis**: Identify recurring expenses, seasonal trends, and spending habits
- **Anomaly Detection**: Detect unusual spending patterns and potential fraud
- **Personalized Recommendations**: AI-powered suggestions for saving money and optimizing budgets
- **Spending Forecasting**: Predict future spending based on historical data
- **Category Analysis**: Break down spending by merchant category
- **Comparative Analysis**: Compare spending to similar users (anonymized)
- **Goal Tracking**: Track progress toward financial goals

## Prerequisites

- Java 17
- Maven 3.9+
- Docker & Docker Compose
- PostgreSQL 16
- Redis 7
- Apache Kafka 3.x

## Technology Stack

- **Framework**: Spring Boot 3.2.5
- **Language**: Java 17
- **Database**: PostgreSQL 16 (Liquibase migrations)
- **Cache**: Redis 7
- **Messaging**: Apache Kafka
- **AI Integration**: AI Orchestration Service (Feature 14)
- **Security**: Spring Security 6 + JWT
- **Observability**: Micrometer + Prometheus + OpenTelemetry

## Quick Start

### 1. Environment Setup

```bash
cp .env.example .env
# Edit .env with your configuration
```

### 2. Start Infrastructure

```bash
docker-compose up -d postgres redis kafka
```

### 3. Build and Run

```bash
mvn clean package
java -jar target/ai-insight-service-1.0.0.jar --spring.profiles.active=dev
```

## API Endpoints

| Method | Path | Auth | Role | Description |
|--------|------|------|------|-------------|
| GET | `/v1/insights` | Yes | USER | Get user's insights |
| GET | `/v1/insights/{id}` | Yes | USER | Get specific insight |
| POST | `/v1/insights/generate` | Yes | USER | Generate new insights |
| GET | `/v1/insights/spending-patterns` | Yes | USER | Get spending patterns |
| GET | `/v1/insights/anomalies` | Yes | USER | Get detected anomalies |
| GET | `/v1/insights/recommendations` | Yes | USER | Get recommendations |
| GET | `/v1/insights/forecast` | Yes | USER | Get spending forecast |
| GET | `/v1/insights/admin` | Yes | ADMIN | Get all insights (admin) |

## Architecture

### Insight Generation Flow

1. **Data Collection**: Fetch user transactions, accounts, and profile
2. **Pattern Analysis**: Analyze spending patterns using statistical methods
3. **AI Processing**: Send analysis to AI Orchestration Service for insights
4. **Anomaly Detection**: Detect unusual spending patterns
5. **Recommendation Generation**: Generate personalized recommendations
6. **Caching**: Cache insights in Redis for fast retrieval
7. **Event Publishing**: Publish Kafka events for analytics
8. **Response**: Return insights to user

### Insight Types

- **SPENDING_PATTERN**: Recurring expenses, trends
- **ANOMALY**: Unusual spending detected
- **RECOMMENDATION**: Actionable suggestions
- **FORECAST**: Future spending predictions
- **COMPARISON**: Peer comparison insights
- **GOAL_PROGRESS**: Financial goal tracking

## Configuration

See `.env.example` for all configuration options.

Key configurations:
- `INSIGHT_GENERATION_ENABLED`: Enable automatic insight generation (default: true)
- `INSIGHT_CACHE_TTL_HOURS`: Cache TTL in hours (default: 24)
- `INSIGHT_MIN_TRANSACTIONS`: Minimum transactions for analysis (default: 10)
- `ANOMALY_DETECTION_THRESHOLD`: Anomaly detection sensitivity (default: 2.0)

## Database Schema

### insights
Stores generated insights with AI analysis results.

### spending_patterns
Tracks identified spending patterns over time.

### recommendations
Stores personalized recommendations.

### anomalies
Records detected spending anomalies.

## Deployment

### Docker Build

```bash
docker build -t ai-insight-service:latest .
```

### Kubernetes/OpenShift

```bash
kubectl apply -f k8s/
```

## Monitoring

- **Health**: `/actuator/health`
- **Metrics**: `/actuator/prometheus`
- **Traces**: Exported to Jaeger/Tempo

## Insight Generation Logic

### Spending Pattern Analysis
- Identifies recurring transactions (monthly, weekly, daily)
- Calculates average spending per category
- Detects seasonal trends
- Compares current vs. historical spending

### Anomaly Detection
- Uses statistical methods (Z-score, IQR)
- Detects outliers in transaction amounts
- Identifies unusual merchant patterns
- Flags suspicious activity

### Recommendation Engine
- Suggests budget optimizations
- Identifies potential savings
- Recommends better financial products
- Provides actionable next steps

## Known Limitations

1. **Data Requirements**: Requires minimum 10 transactions for meaningful insights
2. **AI Latency**: Insight generation can take 5-10 seconds
3. **Cache Staleness**: Cached insights may be up to 24 hours old
4. **Privacy**: Comparative insights use anonymized aggregate data only

## Future Improvements

- Real-time insight generation on transaction creation
- Machine learning models for better predictions
- Multi-currency support
- Investment insights and recommendations
- Tax optimization suggestions

## License

Proprietary - Banking Platform
