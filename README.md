# API Gateway — Banking Platform

## Overview

The API Gateway is the **single ingress point** for all client traffic in the Banking Platform. Every request from the React frontend (and any other client) flows through this service before reaching any downstream microservice.

It is responsible for:
- **JWT authentication** — validates RS256-signed tokens; enriches requests with user identity headers
- **Rate limiting** — Redis-backed sliding window per user (100 req/min) and per IP (200 req/min)
- **Routing** — proxies requests to the correct downstream microservice based on path prefix
- **CORS enforcement** — configurable per environment; no wildcards in production
- **Observability** — structured JSON logging, distributed trace propagation, Prometheus metrics

Built on **Spring Cloud Gateway** (reactive WebFlux / Netty). NOT servlet-based — the reactive stack is required for the concurrency profile of an API gateway layer.

---

## Bounded Context

```
                        ┌─────────────────────────────────┐
Internet / React  ───▶  │         API GATEWAY             │
  Client                │                                 │
                        │  ① JWT Validation               │
                        │  ② Rate Limit Check (Redis)     │
                        │  ③ Route to Downstream Service  │
                        │  ④ Inject X-User-Id headers     │
                        └────────────┬────────────────────┘
                                     │
                    ┌────────────────┼────────────────┐
                    ▼                ▼                 ▼
             Identity Svc     Account Svc      Transaction Svc
             User Svc         Fraud Svc        AI Orchestration
             ...              ...              ...
```

---

## Prerequisites

| Tool          | Version   | Notes                                  |
|---------------|-----------|----------------------------------------|
| Java JDK      | 25        | `java -version` to verify              |
| Maven         | 3.9+      | Or use `./mvnw` (wrapper included)     |
| Docker        | 24+       | For containerised dev                  |
| Docker Compose| 2.x       | For local stack                        |
| Redis         | 7.x       | Via docker-compose or local install    |

---

## Local Setup

### 1. Clone and navigate

```bash
git clone <repo>
cd banking-platform/api-gateway
```

### 2. Configure environment

```bash
cp .env.example .env
```

Edit `.env` — the **only required field** for the gateway to start is `JWT_PUBLIC_KEY`.

For local development, generate a test RSA key pair:

```bash
# Generate 2048-bit RSA private key
openssl genrsa -out private.pem 2048

# Extract public key (this goes in the Gateway .env)
openssl rsa -in private.pem -pubout -out public.pem

# View public key (single-line for .env)
cat public.pem
```

The `private.pem` goes **only** into the Identity Service (Feature 2). The gateway needs only `public.pem`.

### 3. Start the stack

```bash
# Start Redis + API Gateway
docker-compose up -d

# Watch logs
docker-compose logs -f api-gateway

# Verify health
curl http://localhost:8081/actuator/health
```

### 4. Run without Docker (for development)

```bash
# Start Redis only
docker-compose up -d redis

# Set env vars and run Spring Boot
export $(cat .env | xargs)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Gateway will be available at `http://localhost:8080`.

---

## API Summary

| Method | Path                          | Auth Required | Description                                |
|--------|-------------------------------|---------------|--------------------------------------------|
| POST   | `/api/v1/auth/login`          | No            | Proxied to Identity Service                |
| POST   | `/api/v1/auth/register`       | No            | Proxied to Identity Service                |
| POST   | `/api/v1/auth/refresh`        | No            | Token refresh — proxied to Identity Service|
| POST   | `/api/v1/auth/otp/verify`     | No            | OTP verification — proxied to Identity Svc |
| GET    | `/api/v1/accounts/**`         | Yes           | Proxied to Account Service                 |
| GET    | `/api/v1/transactions/**`     | Yes           | Proxied to Transaction Service             |
| POST   | `/api/v1/ai/chat/anonymous`   | No            | Anonymous AI chat — proxied to Chat Service|
| `*`    | `/api/v1/ai/**`               | Yes           | Proxied to AI Orchestration Service        |
| `*`    | `/api/v1/documents/**`        | Yes           | Proxied to Document Ingestion Service      |
| `*`    | `/api/v1/analytics/**`        | Yes           | Proxied to Analytics Service               |
| GET    | `/actuator/health`            | No            | Gateway health (liveness + readiness)      |
| GET    | `/actuator/prometheus`        | No (internal) | Prometheus metrics scrape endpoint         |

**Gateway-injected headers** (forwarded to all downstream services):

| Header          | Content                                      |
|-----------------|----------------------------------------------|
| `X-User-Id`     | JWT subject (userId UUID)                    |
| `X-User-Roles`  | Comma-separated roles from JWT claims        |
| `X-Trace-Id`    | W3C traceparent trace ID                     |
| `X-Gateway-Source` | Always `api-gateway`                      |

---

## Environment Variables Reference

| Variable                        | Default                    | Required | Description                                         |
|---------------------------------|----------------------------|----------|-----------------------------------------------------|
| `JWT_PUBLIC_KEY`                | —                          | **YES**  | RSA public key PEM string for token validation      |
| `JWT_ISSUER`                    | `banking-platform`         | No       | Expected `iss` claim in JWT                         |
| `JWT_AUDIENCE`                  | `banking-api`              | No       | Expected `aud` claim in JWT                         |
| `REDIS_HOST`                    | `localhost`                | No       | Redis server hostname                               |
| `REDIS_PORT`                    | `6379`                     | No       | Redis server port                                   |
| `REDIS_PASSWORD`                | _(empty)_                  | No       | Redis AUTH password                                 |
| `RATE_LIMIT_PER_USER`           | `100`                      | No       | Max requests/min per authenticated user             |
| `RATE_LIMIT_PER_IP`             | `200`                      | No       | Max requests/min per IP address                     |
| `CORS_ALLOWED_ORIGIN_1`         | `http://localhost:3000`    | No       | Frontend origin allowed by CORS                     |
| `IDENTITY_SERVICE_URL`          | `http://localhost:8082`    | No       | Identity Service base URL                           |
| `OTEL_EXPORTER_OTLP_ENDPOINT`   | `http://localhost:4318/...`| No       | OpenTelemetry collector OTLP endpoint               |
| `SPRING_PROFILES_ACTIVE`        | `dev`                      | No       | Active profile: `dev`, `staging`, `prod`            |

Full reference: see `.env.example`

---

## Running Tests

```bash
# Unit tests only (no Redis required)
./mvnw test -Dtest="JwtValidatorTest,RateLimitFilterTest"

# All tests
./mvnw test

# With coverage report
./mvnw verify

# View coverage report
open target/site/jacoco/index.html
```

---

## Sample curl Commands

### Missing token → 401
```bash
curl -i http://localhost:8080/api/v1/accounts/me
# HTTP/1.1 401
# {"success":false,"error":{"code":"MISSING_TOKEN","message":"Authorization header with Bearer token is required"},...}
```

### Expired token → 401
```bash
curl -i http://localhost:8080/api/v1/accounts/me \
  -H "Authorization: Bearer eyJ0eXAiOiJKV1Qi...expired-token..."
# HTTP/1.1 401
# {"success":false,"error":{"code":"INVALID_TOKEN","message":"Token has expired"},...}
```

### Valid token → proxied to downstream (503 if not running)
```bash
curl -i http://localhost:8080/api/v1/accounts/me \
  -H "Authorization: Bearer eyJ0eXAiOiJKV1Qi...valid-token..."
# HTTP/1.1 503 (downstream not running)
# OR HTTP/1.1 200 (downstream running)
```

### Public health check
```bash
curl http://localhost:8081/actuator/health
# {"status":"UP","components":{"redis":{"status":"UP"},...}}
```

### Rate limit hit → 429
```bash
# Send 201 requests rapidly from the same IP
for i in $(seq 1 201); do
  curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8080/api/v1/auth/login -X POST
done
# Last request: 429
# Retry-After: 60
```

---

## Architecture Decisions & Tradeoffs

| Decision                          | Rationale                                                                        | Tradeoff                                                                  |
|-----------------------------------|----------------------------------------------------------------------------------|---------------------------------------------------------------------------|
| Spring Cloud Gateway over Nginx   | Java-native, Spring Security integration, typed filter chain, easy unit testing  | Higher memory than Nginx; adds JVM startup time                           |
| Reactive (WebFlux) stack          | Required for SCG; handles thousands of concurrent connections efficiently         | Debugging reactive chains is harder than blocking servlet code             |
| Rate limit: fail-open on Redis down | Platform availability > strict rate limiting; Redis failures are rare            | DDoS risk if Redis is down; mitigate with aggressive Redis monitoring      |
| JWT validation only (no issuance) | Single responsibility; Identity Service owns the auth domain entirely             | Gateway cannot know if a token was revoked mid-TTL (15 min exposure window)|
| Sliding window (not token bucket) | Simpler Redis Lua script; good enough for our traffic profile                    | Slight overcount at window boundaries; token bucket would be more precise  |
| RSA over HMAC for JWT             | RSA allows public key distribution; gateway can validate without sharing secrets | Slower signing (Identity Service) — acceptable since gateway only verifies |

---

## Known Limitations & Planned Improvements

- **Token revocation**: JWT TTL is 15 minutes. If a token is compromised, it remains valid until expiry. Mitigation: short TTL + Redis-based token denylist (to be added in Phase 13 hardening).
- **Circuit breaker**: Not yet applied per route. Will be added in Feature 33 (Resilience4j phase).
- **Service discovery**: Currently uses static URLs from env vars. Will integrate with K8s DNS natively once all services are deployed.
- **mTLS between services**: Planned for Phase 13. Currently relies on NetworkPolicy for service isolation.
- **WebSocket support**: Not configured. Needed if Chat Service (Feature 16) uses WebSocket for streaming responses.

---

## Deployment

```bash
# Build image
docker build -t banking-platform/api-gateway:1.0.0 .

# Push to registry
docker tag banking-platform/api-gateway:1.0.0 your-registry/banking-platform/api-gateway:1.0.0
docker push your-registry/banking-platform/api-gateway:1.0.0

# Deploy to Kubernetes
kubectl create namespace banking  # if not exists
kubectl apply -f k8s/ -n banking

# Verify rollout
kubectl rollout status deployment/api-gateway -n banking

# Check logs
kubectl logs -f deployment/api-gateway -n banking
```
