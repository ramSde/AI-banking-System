# AI Orchestration Service

## Overview

The AI Orchestration Service is the central intelligence router for the banking platform, responsible for multi-model orchestration, cost control, token budget enforcement, and comprehensive AI usage tracking.

## Features

- **Multi-Model Orchestration**: Automatic fallback chain (OpenAI → Anthropic → Ollama)
- **Cost Control**: Per-user budget enforcement and cost tracking
- **Token Budget Management**: Tier-based token limits with real-time tracking
- **Model Selection**: Intelligent routing based on query complexity and user tier
- **Usage Analytics**: Complete AI usage tracking with metrics and reporting
- **Rate Limiting**: Per-user rate limits to prevent abuse
- **Quota Management**: Daily/monthly quota enforcement
- **Fallback Handling**: Graceful degradation when models are unavailable

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
- **AI Providers**: OpenAI, Anthropic Claude, Ollama
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
java -jar target/ai-orchestration-service-1.0.0.jar --spring.profiles.active=dev
```

## API Endpoints

| Method | Path | Auth | Role | Description |
|--------|------|------|------|-------------|
| POST | `/v1/ai/orchestrate` | Yes | USER | Execute AI request with orchestration |
| GET | `/v1/ai/usage/user` | Yes | USER | Get user's AI usage statistics |
| GET | `/v1/ai/usage/admin` | Yes | ADMIN | Get all users' AI usage |
| GET | `/v1/ai/budget/status` | Yes | USER | Get user's budget status |
| GET | `/v1/ai/models` | Yes | USER | Get available AI models |
| POST | `/v1/ai/models/{id}/enable` | Yes | ADMIN | Enable AI model |
| POST | `/v1/ai/models/{id}/disable` | Yes | ADMIN | Disable AI model |

## Architecture

### AI Orchestration Flow

1. **Request Reception**: User submits AI request via REST API
2. **Authentication**: JWT validation and user identification
3. **Quota Check**: Verify user hasn't exceeded daily/monthly limits
4. **Budget Check**: Ensure user has sufficient budget remaining
5. **Model Selection**: Choose optimal model based on complexity and tier
6. **Execution**: Call selected AI model with circuit breaker protection
7. **Fallback**: Automatic fallback to secondary/tertiary models on failure
8. **Usage Tracking**: Record tokens, cost, latency in database
9. **Event Publishing**: Publish Kafka events for analytics
10. **Response**: Return AI response to user

### Model Fallback Chain

```
Primary: OpenAI GPT-4
    ↓ (on failure)
Secondary: Anthropic Claude
    ↓ (on failure)
Tertiary: Local Ollama
```

### Dependencies

- **Identity Service**: User authentication and tier information
- **User Service**: User profile and tier limits
- **Redis**: Quota tracking and rate limiting
- **Kafka**: Event publishing for analytics

## Configuration

See `.env.example` for all configuration options.

Key configurations:
- `AI_PRIMARY_PROVIDER`: Primary AI provider (default: openai)
- `AI_FALLBACK_ENABLED`: Enable automatic fallback (default: true)
- `AI_BUDGET_ENFORCEMENT`: Enable budget enforcement (default: true)
- `AI_QUOTA_ENFORCEMENT`: Enable quota enforcement (default: true)
- `AI_RATE_LIMIT_PER_MINUTE`: Rate limit per user (default: 60)

## Database Schema

### ai_usage
Stores every AI API call with complete metrics.

### ai_models
Configuration for available AI models.

### ai_budgets
User budget allocations and tracking.

### ai_quotas
User quota limits and current usage.

## Deployment

### Docker Build

```bash
docker build -t ai-orchestration-service:latest .
```

### Kubernetes/OpenShift

```bash
kubectl apply -f k8s/
```

## Monitoring

- **Health**: `/actuator/health`
- **Metrics**: `/actuator/prometheus`
- **Traces**: Exported to Jaeger/Tempo

## Cost Calculation

Costs are calculated based on model pricing:
- **GPT-4**: $0.03/1K input tokens, $0.06/1K output tokens
- **GPT-3.5**: $0.0015/1K input tokens, $0.002/1K output tokens
- **Claude**: $0.008/1K input tokens, $0.024/1K output tokens
- **Ollama**: Free (local)

## User Tiers

- **FREE**: 10,000 tokens/day, GPT-3.5 only
- **BASIC**: 50,000 tokens/day, GPT-3.5 + GPT-4
- **PREMIUM**: 200,000 tokens/day, All models
- **ENTERPRISE**: Unlimited, All models + priority

## Known Limitations

1. **Latency**: Fallback adds latency when primary model fails
2. **Cost Tracking**: Costs are estimates based on token counts
3. **Model Availability**: Dependent on external API availability
4. **Rate Limits**: Subject to provider rate limits

## Future Improvements

- Streaming responses for long-running requests
- Model performance analytics
- Automatic model selection based on historical performance
- Cost optimization recommendations
- Custom model fine-tuning support

## License

Proprietary - Banking Platform
