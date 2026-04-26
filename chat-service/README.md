# Chat Service

## Overview

The Chat Service is a production-grade microservice that provides multi-turn, context-aware conversation management with session persistence. It integrates with AI Orchestration Service and RAG Pipeline Service to deliver intelligent, personalized banking assistance through natural language conversations.

## Bounded Context

The Chat Service owns the conversation domain, managing:
- Chat sessions (multi-turn conversations)
- Chat messages (user inputs and AI responses)
- Message feedback (user ratings and comments)
- Conversation history and context management

## Features

- **Multi-turn Conversations**: Maintains context across multiple message exchanges
- **Session Management**: Create, update, archive, and delete chat sessions
- **AI Integration**: Seamless integration with AI Orchestration and RAG Pipeline services
- **Context-Aware Responses**: Includes conversation history in AI requests
- **Message Feedback**: Collect user feedback on AI responses
- **Rate Limiting**: Protect against abuse with configurable rate limits
- **Streaming Support**: Optional streaming responses for real-time interaction
- **Search**: Search sessions by title
- **Pagination**: All list endpoints support pagination

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
java -jar target/chat-service-1.0.0.jar --spring.profiles.active=dev
```

## API Endpoints

### Chat Sessions

| Method | Path | Auth | Role | Description |
|--------|------|------|------|-------------|
| POST | `/v1/chat/sessions` | Yes | USER | Create new chat session |
| GET | `/v1/chat/sessions/{id}` | Yes | USER | Get session details |
| GET | `/v1/chat/sessions` | Yes | USER | List user sessions |
| GET | `/v1/chat/sessions/status/{status}` | Yes | USER | List sessions by status |
| PUT | `/v1/chat/sessions/{id}` | Yes | USER | Update session |
| DELETE | `/v1/chat/sessions/{id}` | Yes | USER | Delete session |
| POST | `/v1/chat/sessions/{id}/archive` | Yes | USER | Archive session |
| GET | `/v1/chat/sessions/search` | Yes | USER | Search sessions |

### Chat Messages

| Method | Path | Auth | Role | Description |
|--------|------|------|------|-------------|
| POST | `/v1/chat/messages` | Yes | USER | Send message and get AI response |
| GET | `/v1/chat/messages/{id}` | Yes | USER | Get message details |
| GET | `/v1/chat/sessions/{id}/messages` | Yes | USER | List session messages |
| GET | `/v1/chat/sessions/{id}/history` | Yes | USER | Get chat history |
| DELETE | `/v1/chat/messages/{id}` | Yes | USER | Delete message |

### Message Feedback

| Method | Path | Auth | Role | Description |
|--------|------|------|------|-------------|
| POST | `/v1/chat/feedback` | Yes | USER | Submit message feedback |
| GET | `/v1/chat/feedback/{id}` | Yes | USER | Get feedback details |
| GET | `/v1/chat/feedback` | Yes | USER | List user feedback |
| DELETE | `/v1/chat/feedback/{id}` | Yes | USER | Delete feedback |

## Environment Variables

| Variable | Default | Description | Required |
|----------|---------|-------------|----------|
| SPRING_PROFILES_ACTIVE | dev | Active Spring profile | Yes |
| SERVER_PORT | 8086 | Server port | Yes |
| DB_URL | jdbc:postgresql://localhost:5432/chat_db | Database URL | Yes |
| DB_USERNAME | admin | Database username | Yes |
| DB_PASSWORD | admin | Database password | Yes |
| REDIS_HOST | localhost | Redis host | Yes |
| REDIS_PORT | 6379 | Redis port | Yes |
| KAFKA_BOOTSTRAP_SERVERS | localhost:9092 | Kafka servers | Yes |
| JWT_SECRET | - | JWT signing secret | Yes |
| AI_ORCHESTRATION_URL | http://ai-orchestration-service:8084/api | AI Orchestration URL | Yes |
| RAG_PIPELINE_URL | http://rag-pipeline-service:8083/api | RAG Pipeline URL | Yes |
| CHAT_MAX_HISTORY | 20 | Max history messages | No |
| CHAT_SESSION_TIMEOUT | 30 | Session timeout (minutes) | No |
| CHAT_RATE_LIMIT_MESSAGES | 20 | Messages per minute limit | No |

## Sample Requests

### Create Chat Session

```bash
curl -X POST http://localhost:8086/api/v1/chat/sessions \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Financial Planning Discussion",
    "metadata": {
      "category": "financial_planning"
    }
  }'
```

### Send Message

```bash
curl -X POST http://localhost:8086/api/v1/chat/messages \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "sessionId": "123e4567-e89b-12d3-a456-426614174000",
    "content": "What is my current account balance?",
    "includeRagContext": true,
    "streamResponse": false
  }'
```

### Submit Feedback

```bash
curl -X POST http://localhost:8086/api/v1/chat/feedback \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "messageId": "123e4567-e89b-12d3-a456-426614174000",
    "rating": "POSITIVE",
    "comment": "Very helpful response"
  }'
```

## Architecture

### Dependencies

**Upstream Services:**
- AI Orchestration Service: AI model routing and orchestration
- RAG Pipeline Service: Document retrieval and context assembly
- Identity Service: JWT validation

**Infrastructure:**
- PostgreSQL: Session and message persistence
- Redis: Caching and rate limiting
- Kafka: Event publishing

### Integration Flow

1. User sends message via REST API
2. Service validates session and user authorization
3. Service retrieves recent conversation history
4. Service calls AI Orchestration with message + history + RAG context
5. AI Orchestration returns response
6. Service persists user message and AI response
7. Service publishes ChatMessageSent event to Kafka
8. Service returns AI response to user

### Kafka Topics

- `banking.chat.session-created`: Published when session created
- `banking.chat.message-sent`: Published when message sent
- `banking.chat.feedback-submitted`: Published when feedback submitted

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

## Deployment

### Docker Build

```bash
docker build -t chat-service:latest .
```

### Kubernetes/OpenShift

```bash
kubectl apply -f k8s/
```

## Monitoring

- **Health**: `/actuator/health`
- **Metrics**: `/actuator/prometheus`
- **API Docs**: `/swagger-ui.html` (dev only)

## Known Limitations

1. **Context Window**: Limited by AI model's maximum context length
2. **Rate Limiting**: Per-user limits may need tuning based on usage patterns
3. **Session Timeout**: Inactive sessions marked inactive after 30 minutes
4. **Streaming**: Streaming responses not yet implemented

## Future Improvements

- Implement streaming responses for real-time interaction
- Add support for multi-modal inputs (images, voice)
- Implement conversation branching and forking
- Add conversation export functionality
- Implement advanced search with filters
- Add conversation analytics and insights

## License

Proprietary - Banking Platform
