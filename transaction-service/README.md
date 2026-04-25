# Transaction Service

## Overview

The Transaction Service is a production-grade microservice responsible for managing all financial transactions with double-entry ledger bookkeeping, idempotency guarantees, and real-time balance updates. It supports deposits, withdrawals, transfers, payments, refunds, fees, interest, and transaction reversals with complete audit trails.

**Bounded Context**: Transaction Management & Double-Entry Ledger

## Purpose

- Process financial transactions with ACID guarantees
- Maintain double-entry ledger for all transactions
- Ensure idempotency for duplicate request prevention
- Support transaction holds and authorizations
- Enable transaction reversals with audit trail
- Integrate with Account Service for balance updates
- Publish transaction events for downstream services
- Provide comprehensive transaction history and reporting

## Prerequisites

- Java 25
- Maven 3.9+
- Docker & Docker Compose
- PostgreSQL 16
- Redis 7
- Apache Kafka 3.x
- Account Service (running)

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

### 3. Start Account Service

The Transaction Service depends on Account Service for balance operations.

### 4. Build Application

```bash
mvn clean install
```

### 5. Run Application

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

The service will start on `http://localhost:8087/api`

## Key Features

### 1. Double-Entry Ledger
- Every transaction creates two ledger entries (debit + credit)
- Immutable ledger for audit and reconciliation
- Balance tracking before and after each entry

### 2. Idempotency
- Idempotency-Key header required for all state-changing operations
- 24-hour idempotency window
- Cached responses for duplicate requests
- SHA-256 request hash validation

### 3. Transaction Types
- **DEPOSIT**: Money added to account
- **WITHDRAWAL**: Money removed from account
- **TRANSFER**: Money moved between accounts
- **PAYMENT**: Payment to external party
- **REFUND**: Money returned to account
- **FEE**: Service fee charged
- **INTEREST**: Interest credited
- **REVERSAL**: Transaction reversal

### 4. Transaction Lifecycle
- **PENDING**: Initiated but not processed
- **PROCESSING**: Being processed
- **COMPLETED**: Successfully completed
- **FAILED**: Processing failed
- **REVERSED**: Transaction reversed

### 5. Transaction Holds
- Authorization holds for pre-auth
- Reservation holds for specific purposes
- Automatic expiry after configurable period
- Hold capture and release operations

### 6. Account Service Integration
- Real-time balance validation
- Atomic balance updates
- Circuit breaker for resilience
- Retry logic with exponential backoff

## Transaction Types

### Deposit
```json
{
  "transactionType": "DEPOSIT",
  "destinationAccountId": "uuid",
  "amount": 1000.00,
  "currency": "USD",
  "description": "Salary deposit"
}
```

### Withdrawal
```json
{
  "transactionType": "WITHDRAWAL",
  "sourceAccountId": "uuid",
  "amount": 500.00,
  "currency": "USD",
  "description": "ATM withdrawal"
}
```

### Transfer
```json
{
  "transactionType": "TRANSFER",
  "sourceAccountId": "uuid",
  "destinationAccountId": "uuid",
  "amount": 250.00,
  "currency": "USD",
  "description": "Transfer to savings"
}
```

## Business Rules

### Transaction Validation
1. Amount must be positive
2. Source account must have sufficient balance (for debits)
3. Accounts must be active and not frozen
4. Currency must match account currency
5. Daily transaction limits enforced per user
6. Maximum transaction amount enforced

### Idempotency Rules
1. Idempotency-Key header required for POST/PUT
2. Same key + same request = same response (no duplicate)
3. Same key + different request = 409 Conflict
4. Keys expire after 24 hours

### Reversal Rules
1. Only COMPLETED transactions can be reversed
2. Reversal creates new transaction with type REVERSAL
3. Original transaction marked as REVERSED
4. Reversal amount must match original amount
5. Reversal updates balances in opposite direction

## Architecture Decisions

### 1. Double-Entry Ledger
- **Decision**: Separate ledger_entry table for all entries
- **Rationale**: Audit trail, reconciliation, compliance
- **Tradeoff**: Additional storage, more complex queries

### 2. Idempotency Table
- **Decision**: Dedicated idempotency_keys table
- **Rationale**: Prevent duplicate transactions, cache responses
- **Tradeoff**: Additional storage, cleanup required

### 3. Optimistic Locking
- **Decision**: @Version field on Transaction entity
- **Rationale**: Prevent lost updates in concurrent scenarios
- **Tradeoff**: Retry logic required on version conflicts

### 4. Account Service Integration
- **Decision**: Synchronous REST calls with circuit breaker
- **Rationale**: Real-time balance validation required
- **Tradeoff**: Increased latency, dependency on Account Service

### 5. Transaction Holds
- **Decision**: Separate transaction_hold table
- **Rationale**: Support pre-authorization flows
- **Tradeoff**: Additional complexity, expiry management

## Known Limitations

1. **Cross-Currency**: Supported but no real-time exchange rates
2. **Batch Transactions**: Not yet implemented
3. **Scheduled Transactions**: Not yet implemented
4. **Transaction Limits**: Basic implementation, needs enhancement
5. **Hold Expiry**: Manual cleanup required (no automated job)

## Planned Improvements

1. Automated hold expiry cleanup job
2. Batch transaction processing
3. Scheduled/recurring transactions
4. Advanced transaction limits (velocity checks)
5. Real-time fraud detection integration
6. Transaction categorization
7. Multi-currency exchange rate integration
8. Transaction search with full-text indexing

## Kafka Topics

### Produced
- `banking.transaction.transaction-created`
- `banking.transaction.transaction-completed`
- `banking.transaction.transaction-failed`
- `banking.transaction.transaction-reversed`
- `banking.transaction.hold-created`
- `banking.transaction.hold-released`

### Consumed
- `banking.account.account-created` - Account creation events
- `banking.account.status-changed` - Account status changes

## Database Schema

### Tables
- `transaction` - Core transaction records
- `ledger_entry` - Double-entry ledger (immutable)
- `idempotency_keys` - Idempotency tracking
- `transaction_hold` - Authorization holds

### Indexes
- 20+ indexes for performance optimization
- Composite indexes on common query patterns
- Partial indexes for active records

## Monitoring

### Health Checks
- Liveness: `GET /api/actuator/health/liveness`
- Readiness: `GET /api/actuator/health/readiness`

### Metrics
- Prometheus: `GET /api/actuator/prometheus`
- Custom metrics:
  - `banking.transaction.created.total`
  - `banking.transaction.completed.total`
  - `banking.transaction.failed.total`
  - `banking.transaction.processing.duration`
  - `banking.ledger.entries.created.total`

## Security

- JWT RS256 authentication required for all endpoints
- Role-based access control (RBAC)
- Users can only access their own transactions (except admins)
- Admin-only operations: reversal, hold management
- Idempotency-Key validation
- No sensitive data in logs
- Audit trail for all operations

## Integration with Other Services

### Account Service (Synchronous)
- Balance validation before transaction
- Balance update after transaction
- Account status verification
- Circuit breaker: 50% failure rate threshold
- Retry: 3 attempts with exponential backoff

### Fraud Detection Service (Asynchronous)
- Transaction events published to Kafka
- Real-time fraud scoring
- Automatic transaction blocking on high risk

### Audit Service (Asynchronous)
- All transaction events logged
- Before/after state tracking
- User and device information

## Support

For issues or questions, contact the platform team or create an issue in the repository.

---

**Status**: Feature 8 - In Progress (60% Complete)
**Next Steps**: Complete service layer, controllers, DTOs, and deployment manifests
