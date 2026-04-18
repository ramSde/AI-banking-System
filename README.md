# Banking Platform - AI-Powered Microservices Architecture

Production-grade banking platform built with Java 25, Spring Boot 3.x, and AI integration. Designed for high availability, security, and scalability with comprehensive observability.

## 🏗️ Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           BANKING PLATFORM                                  │
├─────────────────────────────────────────────────────────────────────────────┤
│  Frontend (React)  →  API Gateway  →  Microservices  →  Databases/AI       │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────┐    ┌─────────────────┐    ┌─────────────────────────────┐
│   Web Client    │    │  Mobile App     │    │    External APIs            │
└─────────┬───────┘    └─────────┬───────┘    └─────────┬───────────────────┘
          │                      │                      │
          └──────────────────────┼──────────────────────┘
                                 │
                    ┌─────────────▼─────────────┐
                    │       API Gateway         │
                    │  - JWT Authentication     │
                    │  - Rate Limiting          │
                    │  - Request Routing        │
                    │  - Circuit Breaker        │
                    │  - CORS & Security        │
                    └─────────────┬─────────────┘
                                 │
        ┌────────────────────────┼────────────────────────┐
        │                        │                        │
┌───────▼───────┐    ┌───────────▼───────────┐    ┌───────▼───────┐
│ Identity       │    │ Account Service       │    │ Transaction   │
│ Service        │    │                       │    │ Service       │
│ - JWT/OAuth    │    │ - Multi-account       │    │ - Payments    │
│ - MFA/OTP      │    │ - Balance tracking    │    │ - Transfers   │
│ - Risk Auth    │    │ - Account types       │    │ - Ledger      │
└────────────────┘    └───────────────────────┘    └───────────────┘

┌────────────────┐    ┌───────────────────────┐    ┌───────────────┐
│ AI Orchestration│    │ Chat Service          │    │ Fraud         │
│ Service         │    │                       │    │ Detection     │
│ - Multi-model   │    │ - Conversational AI   │    │ - ML Models   │
│ - RAG Pipeline  │    │ - Session mgmt        │    │ - Risk Rules  │
│ - Cost tracking │    │ - Context aware       │    │ - Real-time   │
└────────────────┘    └───────────────────────┘    └───────────────┘
```

## 🚀 Current Status: Feature 1 - API Gateway ✅

**COMPLETED**: Production-grade API Gateway with full implementation

### ✅ What's Implemented

- **Spring Cloud Gateway** with reactive architecture
- **JWT Authentication** with RSA-256 signature validation
- **Rate Limiting** using Redis sliding window algorithm
- **Request Routing** to 12+ downstream microservices
- **Circuit Breaker** patterns for fault tolerance
- **Comprehensive Observability** (metrics, tracing, logging)
- **Security Hardening** (CORS, headers, PII masking)
- **OpenShift Deployment** manifests
- **Docker Compose** for local development
- **Complete Test Suite** (unit + integration)

## 📁 Project Structure

```
banking-platform/
├── pom.xml                          # Parent POM with dependency management
├── README.md                        # This file
├── docker-compose.yml              # Local development infrastructure
│
├── api-gateway/                     # ✅ COMPLETED - Feature 1
│   ├── pom.xml
│   ├── Dockerfile
│   ├── README.md
│   ├── .env.example
│   └── src/
│       ├── main/java/com/banking/gateway/
│       │   ├── ApiGatewayApplication.java
│       │   ├── config/
│       │   │   ├── GatewayProperties.java
│       │   │   ├── GatewayRoutingConfig.java
│       │   │   ├── SecurityConfig.java
│       │   │   └── RedisConfig.java
│       │   ├── filter/
│       │   │   ├── JwtAuthenticationFilter.java
│       │   │   ├── RateLimitFilter.java
│       │   │   └── RequestLoggingFilter.java
│       │   ├── util/
│       │   │   └── JwtValidator.java
│       │   ├── exception/
│       │   │   ├── InvalidTokenException.java
│       │   │   └── RateLimitExceededException.java
│       │   ├── dto/
│       │   │   └── ApiErrorResponse.java
│       │   └── handler/
│       │       └── RedisHealthIndicator.java
│       ├── main/resources/
│       │   ├── application.yml
│       │   ├── application-dev.yml
│       │   ├── application-staging.yml
│       │   ├── application-prod.yml
│       │   └── logback-spring.xml
│       └── test/
│           ├── java/com/banking/gateway/
│           └── resources/application-test.yml
│
├── shared/                          # 🔄 STRUCTURE READY
│   ├── pom.xml
│   ├── common-dto/                  # Shared DTOs and data structures
│   ├── security-lib/                # Common security utilities
│   ├── kafka-lib/                   # Kafka event handling
│   └── logging-lib/                 # Structured logging utilities
│
├── infrastructure/                  # 🔄 READY FOR DEPLOYMENT
│   ├── docker/
│   │   └── docker-compose.yml       # Local development stack
│   ├── openshift/
│   │   ├── api-gateway-deployment.yml
│   │   ├── api-gateway-configmap.yml
│   │   ├── api-gateway-secret.yml
│   │   └── api-gateway-hpa.yml
│   └── k8s/                         # Alternative Kubernetes manifests
│
└── services/                        # 🔮 FUTURE MICROSERVICES
    ├── identity-service/            # Feature 2 - NEXT
    ├── user-service/                # Feature 6
    ├── account-service/             # Feature 7
    ├── transaction-service/         # Feature 8
    ├── fraud-detection-service/     # Feature 9
    ├── audit-service/               # Feature 10
    ├── notification-service/        # Feature 11
    ├── document-ingestion-service/  # Feature 12
    ├── rag-pipeline-service/        # Feature 13
    ├── ai-orchestration-service/    # Feature 14
    ├── ai-insight-service/          # Feature 15
    ├── chat-service/                # Feature 16
    ├── analytics-service/           # Feature 24
    └── statement-service/           # Feature 21
```

## 🛠️ Technology Stack

### Backend Core
- **Java 25** - Latest LTS with modern language features
- **Spring Boot 3.4.x** - Latest stable release
- **Spring Cloud Gateway** - Reactive API gateway
- **Spring Security 6** - Authentication and authorization
- **Spring AI** - AI integration and RAG pipelines

### Data & Messaging
- **PostgreSQL 16** - Primary relational database
- **Redis 7** - Caching, rate limiting, sessions
- **Apache Kafka** - Event streaming and messaging
- **ChromaDB** - Vector database for AI embeddings

### AI & ML
- **OpenAI API** - Large language models
- **Ollama** - Local model deployment option
- **Spring AI** - AI orchestration framework
- **pgvector** - PostgreSQL vector extension

### Infrastructure
- **Docker** - Containerization
- **OpenShift** - Production Kubernetes platform
- **Prometheus** - Metrics collection
- **Grafana** - Monitoring dashboards
- **Jaeger** - Distributed tracing

## 🚦 Getting Started

### Prerequisites

- **Java 25** (JDK 25 or later)
- **Maven 3.9+**
- **Docker & Docker Compose**
- **Git**

### Quick Start

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd banking-platform
   ```

2. **Start infrastructure services**
   ```bash
   docker-compose up -d postgres redis kafka jaeger prometheus
   ```

3. **Configure API Gateway**
   ```bash
   cd api-gateway
   cp .env.example .env
   # Edit .env with your configuration
   ```

4. **Run API Gateway**
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```

5. **Verify deployment**
   ```bash
   curl http://localhost:8081/actuator/health
   ```

### Access Points

- **API Gateway**: http://localhost:8080
- **Health Checks**: http://localhost:8081/actuator/health
- **Prometheus Metrics**: http://localhost:8081/actuator/prometheus
- **Jaeger Tracing**: http://localhost:16686
- **Grafana Dashboards**: http://localhost:3000

## 📋 Feature Development Roadmap

### ✅ Phase 1 - Foundation (COMPLETED)
1. **API Gateway** - Spring Cloud Gateway with JWT auth, rate limiting ✅

### 🔄 Phase 2 - Identity & Security (NEXT)
2. **Identity Service** - JWT + refresh token rotation
3. **OTP & MFA Service** - TOTP + SMS/email OTP
4. **Risk-Based Authentication** - Device + behavior scoring

### 🔮 Phase 3 - User Context
5. **Device Intelligence Service** - Fingerprinting, trust scoring
6. **User Service** - Profile, preferences, KYC status

### 🔮 Phase 4 - Core Banking
7. **Account Service** - Multi-account, balance, IBAN generation
8. **Transaction Service** - Idempotent writes, double-entry ledger

### 🔮 Phase 5 - Safety
9. **Fraud Detection Service** - Rule engine + ML integration
10. **Audit Service** - Immutable event log, compliance

### 🔮 Phase 6+ - AI & Advanced Features
11. **Notification Service** - Email, SMS, push notifications
12. **Document Ingestion** - PDF/image processing
13. **RAG Pipeline** - Retrieval augmented generation
14. **AI Orchestration** - Multi-model coordination
15. **AI Insights** - Personalized financial insights
16. **Chat Service** - Conversational AI interface

## 🔒 Security Features

### Authentication & Authorization
- **JWT RS256** signature validation
- **Role-based access control** (RBAC)
- **Multi-factor authentication** (MFA)
- **Risk-based authentication**
- **Device fingerprinting**

### Data Protection
- **PII encryption** at rest and in transit
- **Data masking** in logs and responses
- **GDPR compliance** with data retention policies
- **Audit trails** for all sensitive operations

### Network Security
- **TLS 1.3** encryption
- **CORS** policy enforcement
- **Rate limiting** per user and IP
- **DDoS protection** via circuit breakers
- **Security headers** (HSTS, CSP, etc.)

## 📊 Observability

### Metrics
- **Prometheus** metrics collection
- **Grafana** dashboards
- **Custom business metrics**
- **SLA/SLO monitoring**

### Logging
- **Structured JSON** logging
- **Correlation IDs** for request tracing
- **PII masking** in log outputs
- **Centralized log aggregation**

### Tracing
- **OpenTelemetry** distributed tracing
- **Jaeger** trace visualization
- **Request flow analysis**
- **Performance bottleneck identification**

## 🏗️ Deployment

### Local Development
```bash
docker-compose up -d
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Staging/Production (OpenShift)
```bash
oc apply -f infrastructure/openshift/
oc get pods -l app=api-gateway
```

### Monitoring
- **Health Checks**: `/actuator/health`
- **Metrics**: `/actuator/prometheus`
- **Readiness**: `/actuator/health/readiness`
- **Liveness**: `/actuator/health/liveness`

## 🧪 Testing Strategy

### Test Pyramid
- **Unit Tests**: Service layer logic (80% coverage target)
- **Integration Tests**: End-to-end API flows
- **Contract Tests**: Service interface validation
- **Performance Tests**: Load and stress testing
- **Security Tests**: Penetration testing

### Test Execution
```bash
# Unit tests
mvn test

# Integration tests
mvn verify

# All tests with coverage
mvn clean verify jacoco:report
```

## 📚 Documentation

- **API Documentation**: OpenAPI 3.0 specifications
- **Architecture Decisions**: ADR documents
- **Deployment Guides**: Environment-specific guides
- **Troubleshooting**: Common issues and solutions

## 🤝 Contributing

1. **Feature Development**: Follow the strict 37-feature sequence
2. **Code Standards**: Java 25 features, Spring Boot best practices
3. **Testing**: Comprehensive test coverage required
4. **Documentation**: Update README and API docs
5. **Security**: Security review for all changes

## 📞 Support

- **Documentation**: See individual service README files
- **Monitoring**: Check Grafana dashboards
- **Logs**: Use correlation IDs for request tracing
- **Health**: Monitor `/actuator/health` endpoints

---

## 🎯 Next Steps

**Ready to proceed to Feature 2: Identity Service**

The API Gateway is now complete and production-ready. The next feature will implement the Identity Service with JWT token generation, refresh token rotation, and user authentication flows.

**Confirmation Required**: Please confirm completion of Feature 1 (API Gateway) before proceeding to Feature 2 (Identity Service).