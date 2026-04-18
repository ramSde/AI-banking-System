# API Gateway

Production-grade API Gateway for the Banking Platform, built with Spring Cloud Gateway and designed for high availability, security, and observability.

## Overview

The API Gateway serves as the single entry point for all client requests to the banking platform microservices. It provides authentication, authorization, rate limiting, request routing, and comprehensive observability.

### Key Features

- **JWT Authentication**: RSA-256 signature validation with role-based access control
- **Rate Limiting**: Redis-based sliding window algorithm (per-user and per-IP)
- **Request Routing**: Intelligent routing to 12+ downstream microservices
- **Circuit Breaker**: Fault tolerance with automatic fallback mechanisms
- **Observability**: Distributed tracing, metrics, and structured logging
- **Security**: CORS handling, security headers, and PII masking
- **High Availability**: Horizontal scaling with load balancing

## Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Web Client    │    │  Mobile App     │    │  External API   │
└─────────┬───────┘    └─────────┬───────┘    └─────────┬───────┘
          │                      │                      │
          └──────────────────────┼──────────────────────┘
                                 │
                    ┌─────────────▼─────────────┐
                    │       API Gateway         │
                    │  - JWT Authentication     │
                    │  - Rate Limiting          │
                    │  - Request Routing        │
                    │  - Circuit Breaker        │
                    └─────────────┬─────────────┘
                                 │
        ┌────────────────────────┼────────────────────────┐
        │                        │                        │
┌───────▼───────┐    ┌───────────▼───────────┐    ┌───────▼───────┐
│ Identity       │    │ Account Service       │    │ Transaction   │
│ Service        │    │                       │    │ Service       │
└────────────────┘    └───────────────────────┘    └───────────────┘
```

## Prerequisites

- **Java 25** (JDK 25 or later)
- **Maven 3.9+**
- **Docker & Docker Compose** (for local development)
- **Redis** (for rate limiting and caching)
- **OpenShift/Kubernetes** (for production deployment)

## Local Development Setup

### 1. Clone and Build

```bash
git clone <repository-url>
cd banking-platform/api-gateway
```

### 2. Environment Configuration

Copy the environment template and configure values:

```bash
cp .env.example .env
# Edit .env with your local configuration
```

### 3. Start Infrastructure

Start required services using Docker Compose:

```bash
cd ../infrastructure/docker
docker-compose up -d postgres redis kafka jaeger prometheus
```

### 4. Run the Application

```bash
# From api-gateway directory
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

The gateway will be available at:
- **API**: http://localhost:8080
- **Management**: http://localhost:8081/actuator

## API Documentation

### Authentication Endpoints (Public)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/auth/login` | User login |
| POST | `/api/v1/auth/register` | User registration |
| POST | `/api/v1/auth/refresh` | Token refresh |
| POST | `/api/v1/auth/forgot-password` | Password reset |
| POST | `/api/v1/auth/verify-otp` | OTP verification |

### Protected Endpoints

All other `/api/v1/*` endpoints require valid JWT authentication.

| Service | Path Pattern | Description |
|---------|--------------|-------------|
| User Service | `/api/v1/users/**` | User profiles and preferences |
| Account Service | `/api/v1/accounts/**` | Account management |
| Transaction Service | `/api/v1/transactions/**` | Payment processing |
| Fraud Service | `/api/v1/fraud/**` | Risk analysis |
| AI Service | `/api/v1/ai/**` | AI-powered insights |
| Chat Service | `/api/v1/chat/**` | Conversational AI |
| Analytics Service | `/api/v1/analytics/**` | Financial analytics |

### Health Check Endpoints

| Method | Path | Description | Auth Required |
|--------|------|-------------|---------------|
| GET | `/actuator/health` | Basic health status | No |
| GET | `/actuator/health/liveness` | Kubernetes liveness probe | No |
| GET | `/actuator/health/readiness` | Kubernetes readiness probe | No |
| GET | `/actuator/prometheus` | Prometheus metrics | No |

## Configuration

### Environment Variables

| Variable | Default | Description | Required |
|----------|---------|-------------|----------|
| `SPRING_PROFILES_ACTIVE` | `dev` | Active Spring profile | Yes |
| `SERVER_PORT` | `8080` | HTTP server port | No |
| `MANAGEMENT_PORT` | `8081` | Management server port | No |
| `REDIS_HOST` | `localhost` | Redis server host | Yes |
| `REDIS_PORT` | `6379` | Redis server port | No |
| `REDIS_PASSWORD` | `` | Redis password | No |
| `JWT_PUBLIC_KEY` | - | RSA public key (PEM format) | Yes |
| `JWT_ISSUER` | `banking-platform` | JWT issuer claim | No |
| `JWT_AUDIENCE` | `banking-api` | JWT audience claim | No |
| `RATE_LIMIT_PER_USER` | `100` | Requests per minute per user | No |
| `RATE_LIMIT_PER_IP` | `200` | Requests per minute per IP | No |
| `CORS_ALLOWED_ORIGIN_1` | `http://localhost:3000` | Allowed CORS origin | No |

### JWT Configuration

The gateway validates JWT tokens using RSA-256 signatures. The public key must be provided in PEM format:

```bash
# Generate RSA key pair (for development only)
openssl genrsa -out private_key.pem 2048
openssl rsa -in private_key.pem -pubout -out public_key.pem

# Set environment variable
export JWT_PUBLIC_KEY="$(cat public_key.pem)"
```

**Production**: Use HashiCorp Vault or OpenShift Secrets for key management.

## Testing

### Unit Tests

```bash
mvn test
```

### Integration Tests

```bash
mvn verify
```

### Test with Docker

```bash
# Start test infrastructure
docker-compose -f ../infrastructure/docker/docker-compose.yml up -d redis

# Run tests
mvn test -Dspring.profiles.active=test
```

## Sample Requests

### 1. Health Check

```bash
curl -X GET http://localhost:8081/actuator/health
```

**Response:**
```json
{
  "status": "UP",
  "components": {
    "redis": {
      "status": "UP",
      "details": {
        "responseTime": "2ms"
      }
    }
  }
}
```

### 2. Authentication (Public)

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123"
  }'
```

**Response:**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 900
  },
  "traceId": "550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2024-01-01T12:00:00Z"
}
```

### 3. Protected Endpoint

```bash
curl -X GET http://localhost:8080/api/v1/accounts \
  -H "Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### 4. Rate Limit Exceeded

```bash
# After exceeding rate limit
curl -X GET http://localhost:8080/api/v1/accounts
```

**Response (429):**
```json
{
  "success": false,
  "error": {
    "code": "RATE_LIMIT_EXCEEDED",
    "message": "Too many requests. Please try again later."
  },
  "traceId": "550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2024-01-01T12:00:00Z"
}
```

### 5. Unauthorized Access

```bash
curl -X GET http://localhost:8080/api/v1/accounts
```

**Response (401):**
```json
{
  "success": false,
  "error": {
    "code": "UNAUTHORIZED",
    "message": "Missing authentication token"
  },
  "traceId": "550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2024-01-01T12:00:00Z"
}
```

## Monitoring and Observability

### Metrics

Prometheus metrics are available at `/actuator/prometheus`:

- `http_server_requests_seconds` - HTTP request duration
- `jvm_memory_used_bytes` - JVM memory usage
- `redis_commands_processed_total` - Redis command count
- `gateway_requests_total` - Gateway request count by route

### Distributed Tracing

All requests are traced using OpenTelemetry. Traces are exported to Jaeger:

- **Jaeger UI**: http://localhost:16686
- **Trace ID**: Included in all API responses

### Logging

Structured JSON logging in production:

```json
{
  "timestamp": "2024-01-01T12:00:00.000Z",
  "level": "INFO",
  "logger": "com.banking.gateway.filter.RequestLoggingFilter",
  "message": "Incoming request - Method: GET, Path: /api/v1/accounts",
  "traceId": "550e8400-e29b-41d4-a716-446655440000",
  "userId": "user123"
}
```

## Deployment

### Docker

```bash
# Build image
docker build -t banking/api-gateway:latest .

# Run container
docker run -p 8080:8080 -p 8081:8081 \
  --env-file .env \
  banking/api-gateway:latest
```

### OpenShift

```bash
# Apply all manifests
oc apply -f ../infrastructure/openshift/

# Check deployment status
oc get pods -l app=api-gateway
oc get routes api-gateway-route
```

## Architecture Decisions

### 1. Spring Cloud Gateway vs Zuul

**Decision**: Spring Cloud Gateway
**Reasoning**: 
- Reactive (non-blocking) architecture for better performance
- Native Spring Boot 3.x support
- Better integration with Spring Security
- Active development and long-term support

### 2. Redis for Rate Limiting

**Decision**: Redis sliding window algorithm
**Reasoning**:
- Distributed rate limiting across multiple gateway instances
- More accurate than fixed window algorithms
- High performance with sub-millisecond latency
- Built-in TTL for automatic cleanup

### 3. JWT RSA-256 Signatures

**Decision**: RSA-256 instead of HMAC
**Reasoning**:
- Public key verification (no shared secrets)
- Better security for distributed systems
- Supports key rotation without service restarts
- Industry standard for microservices

## Known Limitations

1. **Circuit Breaker**: Currently configured but fallback responses are generic
2. **Request Transformation**: Limited request/response transformation capabilities
3. **WebSocket Support**: Not implemented (HTTP/REST only)
4. **GraphQL**: No native GraphQL gateway support

## Planned Improvements

1. **Enhanced Fallback Responses**: Service-specific fallback data
2. **Request Caching**: Redis-based response caching for read operations
3. **API Versioning**: Support for multiple API versions simultaneously
4. **Advanced Security**: Request signing and API key authentication
5. **Performance**: Connection pooling optimization and request batching

## Troubleshooting

### Common Issues

1. **Redis Connection Failed**
   ```bash
   # Check Redis connectivity
   redis-cli -h localhost -p 6379 ping
   ```

2. **JWT Validation Failed**
   ```bash
   # Verify public key format
   openssl rsa -pubin -in public_key.pem -text -noout
   ```

3. **High Memory Usage**
   ```bash
   # Check JVM heap usage
   curl http://localhost:8081/actuator/metrics/jvm.memory.used
   ```

### Debug Mode

Enable debug logging for troubleshooting:

```bash
export LOG_LEVEL_GATEWAY=DEBUG
export LOG_LEVEL_SCG=DEBUG
mvn spring-boot:run
```

## Support

For issues and questions:
- **Documentation**: See `/docs` directory
- **Monitoring**: Check Grafana dashboards
- **Logs**: Use correlation ID for request tracing
- **Health**: Monitor `/actuator/health` endpoint