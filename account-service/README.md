# Account Service

## Overview

The Account Service is a production-grade microservice responsible for managing bank accounts, balances, account types (SAVINGS, CHECKING, CREDIT), and IBAN/account number generation. It provides multi-account support with real-time balance tracking, account lifecycle management, and comprehensive audit trails.

**Bounded Context**: Account Management & Balance Operations

## Purpose

- Create and manage multiple accounts per user
- Support different account types with specific rules (SAVINGS, CHECKING, CREDIT)
- Generate unique account numbers and IBANs
- Track account balances with atomic operations
- Maintain immutable audit trail of all balance changes
- Manage account lifecycle (ACTIVE, INACTIVE, FROZEN, CLOSED)
- Enforce business rules (minimum balance, max accounts per user)
- Provide real-time balance information with Redis caching

## Prerequisites

- Java 25
- Maven 3.9+
- Docker & Docker Compose
- PostgreSQL 16
- Redis 7
- Apache Kafka 3.x

## Local Setup

### 1. Environment Configuration

Copy the example environment file:

```bash
cp .env.example .env
```

Edit `.env` with your local configuration.

### 2. Start Infrastructure

```bash
docker-compose up -d postgres redis kafka
```

### 3. Build Application

```bash
mvn clean install
```

### 4. Run Application

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

The service will start on `http://localhost:8086/api`

## API Endpoints

| Method | Path | Auth Required | Role | Description |
|--------|------|---------------|------|-------------|
| POST | `/v1/accounts` | Yes | USER, ADMIN | Create new account |
| GET | `/v1/accounts/my-accounts` | Yes | USER, ADMIN | Get own accounts |
| GET | `/v1/accounts/{id}` | Yes | USER, ADMIN | Get account by ID |
| GET | `/v1/accounts/number/{accountNumber}` | Yes | USER, ADMIN | Get account by account number |
| PUT | `/v1/accounts/{id}` | Yes | USER, ADMIN | Update account settings |
| DELETE | `/v1/accounts/{id}` | Yes | USER, ADMIN | Close account |
| GET | `/v1/accounts` | Yes | ADMIN | List all accounts (paginated) |
| PUT | `/v1/accounts/{id}/status` | Yes | ADMIN | Update account status |
| PUT | `/v1/accounts/{id}/freeze` | Yes | ADMIN | Freeze account |
| PUT | `/v1/accounts/{id}/unfreeze` | Yes | ADMIN | Unfreeze account |

## Environment Variables

| Variable | Default | Description | Required |
|----------|---------|-------------|----------|
| SERVER_PORT | 8086 | Server port | No |
| DB_URL | jdbc:postgresql://localhost:5432/account_db | Database URL | Yes |
| DB_USERNAME | admin | Database username | Yes |
| DB_PASSWORD | admin | Database password | Yes |
| REDIS_HOST | localhost | Redis host | Yes |
| REDIS_PORT | 6379 | Redis port | Yes |
| KAFKA_BOOTSTRAP_SERVERS | localhost:9092 | Kafka servers | Yes |
| ACCOUNT_CACHE_BALANCE_TTL | 5 | Balance cache TTL (minutes) | No |
| ACCOUNT_CACHE_DETAILS_TTL | 15 | Account details cache TTL (minutes) | No |
| ACCOUNT_NUMBER_PREFIX | ACC | Account number prefix | No |
| ACCOUNT_IBAN_COUNTRY_CODE | US | IBAN country code | No |
| ACCOUNT_IBAN_BANK_CODE | BANK | IBAN bank code | No |
| ACCOUNT_DEFAULT_CURRENCY | USD | Default currency | No |
| ACCOUNT_MIN_BALANCE_SAVINGS | 100.00 | Minimum balance for savings accounts | No |
| ACCOUNT_MIN_BALANCE_CHECKING | 0.00 | Minimum balance for checking accounts | No |
| ACCOUNT_MAX_ACCOUNTS_PER_USER | 10 | Maximum accounts per user | No |
| JWT_SECRET | (256 bits) | JWT secret key | Yes |

## Account Types

### SAVINGS
- Earns interest
- Minimum balance: $100.00 (configurable)
- Suitable for long-term savings

### CHECKING
- For daily transactions
- Minimum balance: $0.00 (configurable)
- Supports overdraft protection

### CREDIT
- Allows negative balance up to credit limit
- No minimum balance requirement
- Interest charged on outstanding balance

## Account Status Lifecycle

1. **ACTIVE** - Account is operational, all transactions allowed
2. **INACTIVE** - Temporarily inactive, can be reactivated
3. **FROZEN** - Frozen by admin, no transactions allowed
4. **CLOSED** - Permanently closed, cannot be reopened

## Business Rules

1. **Account Creation**
   - Maximum 10 accounts per user (configurable)
   - Initial deposit must meet minimum balance requirement
   - Unique account number and IBAN generated automatically

2. **Account Closure**
   - Balance must be zero before closure
   - Closed accounts cannot be reopened
   - Soft delete after closure for audit trail

3. **Balance Operations**
   - All amounts use BigDecimal with scale=2, HALF_UP rounding
   - Optimistic locking prevents concurrent balance updates
   - Immutable audit trail for all balance changes

4. **Caching Strategy**
   - Balance: 5-minute TTL (hot data)
   - Account details: 15-minute TTL
   - Cache invalidation on updates

## Architecture Decisions

### 1. Account Number Generation
- **Decision**: Custom format `ACC-{8-digit-random}` with uniqueness check
- **Rationale**: Simple, readable, collision-resistant
- **Tradeoff**: Requires database lookup for uniqueness validation

### 2. IBAN Generation
- **Decision**: ISO 13616 standard with check digit calculation
- **Rationale**: International standard, fraud prevention
- **Tradeoff**: Simplified implementation (full country-specific rules not implemented)

### 3. Balance Tracking
- **Decision**: Three balance fields (balance, available_balance, hold_balance)
- **Rationale**: Support for holds/pending transactions
- **Tradeoff**: Additional complexity in balance calculations

### 4. Optimistic Locking
- **Decision**: @Version field on Account entity
- **Rationale**: Prevent lost updates in concurrent scenarios
- **Tradeoff**: Retry logic required on version conflicts

### 5. Immutable Audit Trail
- **Decision**: Separate AccountBalanceHistory table, no updates/deletes
- **Rationale**: Compliance, dispute resolution, reconciliation
- **Tradeoff**: Storage growth over time

## Known Limitations

1. **IBAN Validation**: Simplified implementation, not all country-specific rules enforced
2. **Multi-Currency**: Supported but no real-time exchange rates
3. **Interest Calculation**: Interest rate stored but calculation not automated
4. **Overdraft**: Limit stored but enforcement delegated to Transaction Service

## Planned Improvements

1. Full ISO 13616 IBAN validation with country-specific rules
2. Automated interest calculation and crediting
3. Real-time multi-currency exchange rate integration
4. Advanced balance holds with expiration
5. Account statements generation
6. Scheduled account maintenance (dormant account detection)

## Kafka Topics

### Produced
- `banking.account.account-created` - Account created
- `banking.account.account-updated` - Account settings updated
- `banking.account.status-changed` - Account status changed
- `banking.account.account-closed` - Account closed

### Consumed
- None (Account Service is a producer-only service)

## Database Schema

### Tables
- `account` - Core account information with balances
- `account_balance_history` - Immutable audit trail of balance changes

### Indexes
- `idx_account_user_id` - Fast user account lookups
- `idx_account_account_number` - Unique account number lookups
- `idx_account_iban` - IBAN lookups
- `idx_account_status` - Status-based queries
- `idx_balance_history_account_id` - Balance history by account

## Monitoring

### Health Checks
- Liveness: `GET /api/actuator/health/liveness`
- Readiness: `GET /api/actuator/health/readiness`

### Metrics
- Prometheus: `GET /api/actuator/prometheus`
- Custom metrics:
  - `banking.account.created.total`
  - `banking.account.closed.total`
  - `banking.account.balance.operations`

## Security

- JWT RS256 authentication required for all endpoints
- Role-based access control (RBAC)
- Users can only access their own accounts (except admins)
- Admin-only operations: freeze, unfreeze, status updates
- No sensitive data in logs
- Audit trail for all operations

## Testing

Run unit tests:
```bash
mvn test
```

Run integration tests:
```bash
mvn verify
```

## Docker Build

```bash
docker build -t account-service:latest .
```

## Kubernetes Deployment

```bash
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/hpa.yaml
```

## Support

For issues or questions, contact the platform team or create an issue in the repository.
