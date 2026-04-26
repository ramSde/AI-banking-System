# API Gateway

## Overview

The API Gateway is the single entry point for all client requests to the Banking Platform. It provides centralized routing, authentication, rate limiting, and observability for all 30+ microservices.

**Bounded Context**: Infrastructure / Gateway Layer

**Technology Stack**:
- Spring Cloud Gateway (reactive, non-blocking)
- Redis (rate limiting, caching)
- JWT RS256 (authentication)
- Resilience4j (circuit breaker, retry)
- Micrometer + OpenTelemetry (observability)

## Features

- **Centralized Routing**: Routes requests to 30+ downstream microservices
- **JWT Authentication**: RS256 signature verification with public key
- **Rate Limiting**: Sliding window algorithm (per-user and per-IP)
- **Request Logging**: Structured JSON logs with distributed tracing
- **Circuit Breaker**: Automatic failover and retry mechanisms
- **CORS Configuration**: Configurable allowed origins per environment
- **Health Checks**: Kubernetes-ready liveness and readiness probes
- **Observability**: Prometheus metrics, OpenTelemetry tracing

## Prerequisites

- Java 17
- Maven 3.9+
- Docker & Docker Compose
- Redis 7+
- Access to Identity Service (for JWT public key)

## Local Setup

### 1. Environment Configuration

Copy the example environment file:

```bash
cp .env.example .env
```

Edit `.env` and configure:
- Redis connection details
- JWT public key (obtain from Identity Service)
- Service URIs for all downstream microservices
- CORS allowed origins

### 2. Start Infrastructure

Start Redis using Docker Compose:

```bash
docker-compose up -d redis
```

### 3. Build and Run

Build the application:

```bash
mvn clean package
```

Run the application:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Or run the JAR directly:

```bash
java -jar target/api-gateway.jar --spring.profiles.active=dev
```

The gateway will start on `http://localhost:8080`

### 4. Verify Health

Check health status:

```bash
curl http://localhost:8080/actuator/health
```

Expected response:
```json
{
  "status": "UP",
  "components": {
    "redis": {
      "status": "UP",
      "details": {
        "redis": "available"
      }
    }
  }
}
```

## API Endpoints

| Method | Path | Auth Required | Description |
|--------|------|---------------|-------------|
| GET | `/actuator/health` | No | Health check endpoint |
| GET | `/actuator/health/liveness` | No | Kubernetes liveness probe |
| GET | `/actuator/health/readiness` | No | Kubernetes readiness probe |
| GET | `/actuator/metrics` | No | Prometheus metrics |
| GET | `/actuator/prometheus` | No | Prometheus scrape endpoint |
| POST | `/api/v1/auth/**` | No | Authentication endpoints (proxied to Identity Service) |
| ALL | `/api/v1/**` | Yes | All other endpoints require JWT authentication |

## Environment Variables

| Variable | Default | Description | Required |
|----------|---------|-------------|----------|
| `SERVER_PORT` | 8080 | Server port | No |
| `SPRING_PROFILES_ACTIVE` | dev | Active Spring profile (dev/staging/prod) | Yes |
| `REDIS_HOST` | localhost | Redis host | Yes |
| `REDIS_PORT` | 6379 | Redis port | Yes |
| `REDIS_PASSWORD` | | Redis password | No |
| `JWT_PUBLIC_KEY` | | JWT RS256 public key (PEM format) | Yes |
| `JWT_ISSUER` | banking-platform | Expected JWT issuer | Yes |
| `JWT_AUDIENCE` | banking-api | Expected JWT audience | Yes |
| `RATE_LIMIT_ENABLED` | true | Enable rate limiting | No |
| `RATE_LIMIT_PER_USER` | 100 | Requests per minute per user | No |
| `RATE_LIMIT_PER_IP` | 200 | Requests per minute per IP | No |
| `CORS_ALLOWED_ORIGINS` | http://localhost:3000 | Comma-separated allowed origins | Yes |
| `OTLP_ENDPOINT` | http://localhost:4317 | OpenTelemetry collector endpoint | No |

## Running Tests

Run all tests:

```bash
mvn test
```

Run integration tests only:

```bash
mvn test -Dtest=*IntegrationTest
```

Run unit tests only:

```bash
mvn test -Dtest=*Test -Dtest=!*IntegrationTest
```

## Architecture Decisions

### 1. Spring Cloud Gateway (Reactive)

**Decision**: Use Spring Cloud Gateway instead of Zuul or traditional servlet-based gateway.

**Reasoning**:
- Non-blocking, reactive architecture for better throughput
- Native Spring Boot 3.x support
- Built-in circuit breaker and retry mechanisms
- Better performance under high load

**Tradeoffs**:
- Reactive programming model has steeper learning curve
- Debugging reactive streams can be more complex

### 2. Redis Sliding Window Rate Limiting

**Decision**: Implement sliding window algorithm using Redis.

**Reasoning**:
- More accurate than fixed window (no burst at window boundaries)
- Distributed rate limiting across multiple gateway instances
- Low latency (< 5ms per check)

**Tradeoffs**:
- Requires Redis availability (added dependency)
- Slightly more complex than fixed window

### 3. JWT RS256 (Public Key Verification)

**Decision**: Use RS256 instead of HS256 for JWT verification.

**Reasoning**:
- Gateway only needs public key (no shared secret)
- Identity Service can rotate private key without updating gateway
- Better security (asymmetric cryptography)

**Tradeoffs**:
- Slightly slower verification (< 1ms difference)
- Requires public key distribution mechanism

### 4. Global Filters vs Route-Specific Filters

**Decision**: Use global filters for authentication, rate limiting, and logging.

**Reasoning**:
- Consistent behavior across all routes
- Simpler configuration
- Easier to maintain

**Tradeoffs**:
- Less flexibility for route-specific customization
- All routes share same filter chain

## Known Limitations

1. **Rate Limiting Accuracy**: Sliding window algorithm is approximate (not exact) due to Redis key expiration
2. **JWT Public Key Rotation**: Requires gateway restart to load new public key (future: implement hot reload)
3. **Circuit Breaker State**: Not shared across gateway instances (each instance maintains own state)
4. **Request Body Logging**: Disabled for performance (only headers and metadata logged)

## Planned Improvements

1. **Dynamic Routing**: Load routes from configuration service instead of static YAML
2. **JWT Public Key Hot Reload**: Fetch public key from Identity Service on startup and periodically refresh
3. **Advanced Rate Limiting**: Support for different limits per endpoint or user tier
4. **Request/Response Transformation**: Add filters for request/response body transformation
5. **API Versioning**: Support for multiple API versions with automatic routing

## Deployment

### Docker

Build Docker image:

```bash
docker build -t api-gateway:latest .
```

Run Docker container:

```bash
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e REDIS_HOST=redis \
  -e JWT_PUBLIC_KEY="$(cat public-key.pem)" \
  api-gateway:latest
```

### Kubernetes/OpenShift

Apply Kubernetes manifests:

```bash
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/hpa.yaml
```

For OpenShift, also apply the Route:

```bash
oc apply -f k8s/route.yaml
```

### Observability

**Prometheus Metrics**: Available at `/actuator/prometheus`

**Key Metrics**:
- `http_server_requests_seconds`: Request latency histogram
- `gateway_requests_total`: Total requests counter
- `gateway_rate_limit_exceeded_total`: Rate limit violations
- `redis_commands_total`: Redis command counter

**Distributed Tracing**: Traces exported to OTLP endpoint (Jaeger/Tempo)

**Logs**: Structured JSON logs with traceId, spanId, userId fields

## Support

For issues or questions, contact the Banking Platform Team.

**Version**: 1.0.0  
**Last Updated**: 2024-01-01
