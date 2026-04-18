# API Gateway

Production-grade API Gateway for the Banking Platform microservices ecosystem. Built on Spring Cloud Gateway with reactive architecture, providing centralized authentication, rate limiting, request routing, and comprehensive observability.

## Purpose & Bounded Context

The API Gateway serves as the single entry point for all client requests, handling:
- JWT-based authentication and authorization
- Redis-based rate limiting with sliding window algorithm
- Request routing to 12+ downstream banking services
- Circuit breaker patterns for fault tolerance
- Comprehensive request/response logging with PII masking
- CORS handling for web clients

**Bounded Context:** Gateway & Routing Domain - responsible for request ingress, security enforcement, and traffic management.

## Prerequisites

- Java 25
- Docker & Docker Compose
- Maven 3.9+
- Redis (for rate limiting)
- Access to downstream banking services

## Local Setup

### 1. Environment Configuration

Copy the environment template and customize:

```bash
cp .env.example .env
# Edit .env with your local configuration
```

### 2. Generate JWT Keys (Development)

```bash
# Generate RSA key pair for JWT validation
openssl genrsa -out private.pem 2048
openssl rsa -in private.pem -pubout -out public.pem

# Copy public key content to .env JWT_PUBLIC_KEY variable
```

### 3. Start Dependencies

```bash
# Start Redis and other dependencies
docker-compose up -d redis
```

### 4. Run the Application

```bash
# Using Maven
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Or using JAR
mvn clean package -DskipTests
java -jar target/api-gateway-*.jar --spring.profiles.active=dev
```

### 5. Verify Health

```bash
curl http://localhost:8080/actuator/health
```

## API Summary

| Method | Path | Auth Required | Role | Description |
|--------|------|---------------|------|-------------|
| GET | `/actuator/health` | No | - | Health check endpoint |
| GET | `/actuator/metrics` | No | - | Prometheus metrics |
| GET | `/actuator/prometheus` | No | - | Prometheus scraping endpoint |
| ALL | `/api/v1/auth/**` | No | - | Identity service routes (public) |
| ALL | `/api/v1/users/**` | Yes | USER | User service routes |
| ALL | `/api/v1/accounts/**` | Yes | USER | Account service routes |
| ALL | `/api/v1/transactions/**` | Yes | USER | Transaction service routes |
| ALL | `/api/v1/fraud/**` | Yes | ADMIN | Fraud detection routes |
| ALL | `/api/v1/audit/**` | Yes | ADMIN | Audit service routes |
| ALL | `/api/v1/notifications/**` | Yes | USER | Notification service routes |
| ALL | `/api/v1/chat/**` | Yes | USER | Chat service routes |
| ALL | `/api/v1/ai/**` | Yes | USER | AI orchestration routes |
| ALL | `/api/v1/documents/**` | Yes | USER | Document ingestion routes |
| ALL | `/api/v1/analytics/**` | Yes | USER | Analytics service routes |
| ALL | `/api/v1/statements/**` | Yes | USER | Statement service routes |

## Environment Variables

| Variable | Default | Description | Required |
|----------|---------|-------------|----------|
| `SPRING_PROFILES_ACTIVE` | `dev` | Active Spring profile | Yes |
| `SERVER_PORT` | `8080` | HTTP server port | No |
| `REDIS_HOST` | `localhost` | Redis server hostname | Yes |
| `REDIS_PORT` | `6379` | Redis server port | No |
| `REDIS_PASSWORD` | `` | Redis authentication password | No |
| `JWT_PUBLIC_KEY` | - | RSA public key for JWT validation (PEM format) | Yes |
| `JWT_ISSUER` | `banking-platform-dev` | Expected JWT issuer | Yes |
| `JWT_AUDIENCE` | `banking-api-dev` | Expected JWT audience | Yes |
| `RATE_LIMIT_PER_USER` | `1000` | Requests per minute per user (dev) | No |
| `RATE_LIMIT_PER_IP` | `2000` | Requests per minute per IP (dev) | No |
| `CORS_ALLOWED_ORIGIN_1` | `http://localhost:3000` | Allowed CORS origin | Yes |
| `IDENTITY_SERVICE_URL` | `http://localhost:8082` | Identity service URL | Yes |
| `USER_SERVICE_URL` | `http://localhost:8083` | User service URL | Yes |
| `ACCOUNT_SERVICE_URL` | `http://localhost:8084` | Account service URL | Yes |
| `TRANSACTION_SERVICE_URL` | `http://localhost:8085` | Transaction service URL | Yes |
| `OTEL_EXPORTER_OTLP_ENDPOINT` | `http://localhost:4318/v1/traces` | OpenTelemetry endpoint | No |

## Running Tests

```bash
# Unit tests only
mvn test

# Integration tests only
mvn failsafe:integration-test

# All tests
mvn verify

# With coverage report
mvn clean verify jacoco:report
```

## Architecture Decisions

### 1. Reactive Architecture
- **Decision:** Use Spring Cloud Gateway (WebFlux) instead of Zuul
- **Reasoning:** Better performance, non-blocking I/O, native Spring Cloud integration
- **Tradeoff:** Steeper learning curve, reactive programming complexity

### 2. Redis Rate Limiting
- **Decision:** Implement sliding window rate limiting with Redis
- **Reasoning:** Distributed rate limiting, high performance, atomic operations
- **Tradeoff:** Redis dependency, network latency for each request

### 3. JWT Validation at Gateway
- **Decision:** Validate JWT tokens at gateway level
- **Reasoning:** Centralized security, reduced load on downstream services
- **Tradeoff:** Single point of failure, requires public key distribution

### 4. Circuit Breaker Pattern
- **Decision:** Use Resilience4j circuit breakers for all downstream services
- **Reasoning:** Fault tolerance, prevent cascade failures, graceful degradation
- **Tradeoff:** Additional complexity, configuration overhead

### 5. Structured JSON Logging
- **Decision:** Use structured JSON logging in production
- **Reasoning:** Better log aggregation, searchability, monitoring integration
- **Tradeoff:** Larger log size, requires log parsing infrastructure

## Known Limitations

1. **JWT Key Rotation:** Manual public key updates required (no automatic rotation)
2. **Rate Limiting Precision:** Redis-based sliding window has minor timing precision issues
3. **Circuit Breaker State:** Circuit breaker state not shared across gateway instances
4. **CORS Preflight:** Complex CORS scenarios may require additional configuration
5. **Request Size:** Large request bodies (>10MB) may cause memory pressure

## Planned Improvements

1. **Automatic JWT Key Rotation:** Integration with HashiCorp Vault or JWKS endpoint
2. **Advanced Rate Limiting:** Token bucket algorithm, burst handling
3. **Request Caching:** Intelligent response caching for read-heavy endpoints
4. **Request Transformation:** Request/response transformation capabilities
5. **Advanced Monitoring:** Custom business metrics, SLA monitoring
6. **Load Balancing:** Weighted routing, canary deployments
7. **API Versioning:** Automatic API version routing and deprecation handling

## Monitoring & Observability

### Metrics (Prometheus)
- Request count by endpoint and status code
- Request duration histograms
- Rate limit violations
- Circuit breaker state changes
- Redis connection pool metrics

### Health Checks
- `/actuator/health/liveness` - Kubernetes liveness probe
- `/actuator/health/readiness` - Kubernetes readiness probe
- Custom Redis connectivity check

### Distributed Tracing
- OpenTelemetry integration
- Trace ID propagation to downstream services
- Jaeger/Tempo export support

### Logging
- Structured JSON logs in production
- Request/response correlation IDs
- PII masking for sensitive data
- Configurable log levels per environment

## Security Considerations

1. **JWT Validation:** RSA-256 signature verification with public key
2. **Rate Limiting:** Per-user and per-IP limits to prevent abuse
3. **CORS Policy:** Strict origin whitelisting in production
4. **Security Headers:** XSS protection, frame options, HSTS
5. **PII Masking:** Automatic masking in logs and error responses
6. **Input Validation:** Request size limits, header validation
7. **Error Handling:** No sensitive information in error responses

## Troubleshooting

### Common Issues

1. **Redis Connection Failed**
   ```bash
   # Check Redis connectivity
   redis-cli -h $REDIS_HOST -p $REDIS_PORT ping
   ```

2. **JWT Validation Failed**
   ```bash
   # Verify public key format
   openssl rsa -pubin -in public.pem -text -noout
   ```

3. **Rate Limit Not Working**
   ```bash
   # Check Redis keys
   redis-cli keys "rate_limit:*"
   ```

4. **Circuit Breaker Open**
   ```bash
   # Check actuator metrics
   curl http://localhost:8080/actuator/metrics/resilience4j.circuitbreaker.state
   ```

### Debug Mode

```bash
# Enable debug logging
export LOG_LEVEL_GATEWAY=DEBUG
export LOG_LEVEL_SCG=DEBUG
```

### Performance Tuning

```bash
# JVM tuning for high load
export JAVA_OPTS="-Xms1g -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=100"

# Netty tuning
export SERVER_NETTY_CONNECTION_TIMEOUT_MS=5000
export GATEWAY_HTTP_POOL_MAX_CONNECTIONS=1000
```