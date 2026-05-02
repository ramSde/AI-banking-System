# I18n Service (Multi-language Support)

## Overview

The I18n Service is a production-grade microservice that provides comprehensive multi-language support for the banking platform. It manages translations, locales, and message bundles, enabling the platform to serve users in multiple languages with dynamic content translation and locale-specific formatting.

## Bounded Context

The I18n Service owns the internationalization domain, managing:
- Translation keys and their translations across multiple locales
- Supported locales configuration and management
- Message bundles for frontend consumption
- Auto-translation integration (optional)
- Translation quality and review workflow

## Features

- **Multi-locale Support**: 7 languages out of the box (en, es, fr, de, hi, ar, zh)
- **Dynamic Translations**: Support for placeholder replacement in translated strings
- **Fallback Mechanism**: Automatic fallback to default locale when translation missing
- **Caching**: Redis-based caching for high-performance translation retrieval
- **Auto-translation**: Optional integration with translation APIs (Google, DeepL)
- **Translation Quality**: Quality scoring and review workflow
- **Message Bundles**: Bulk translation retrieval for frontend applications
- **RTL Support**: Right-to-left language support (Arabic)
- **Category-based Organization**: Translations organized by categories

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
java -jar target/i18n-service-1.0.0.jar --spring.profiles.active=dev
```

## API Endpoints

### Translation APIs

| Method | Path | Auth | Role | Description |
|--------|------|------|------|-------------|
| POST | `/v1/i18n/translate` | Yes | USER | Translate single key |
| POST | `/v1/i18n/translate/bulk` | Yes | USER | Translate multiple keys |
| GET | `/v1/i18n/translate/{keyName}/{localeCode}` | Yes | USER | Get translation |
| GET | `/v1/i18n/locales/{localeCode}/translations` | Yes | USER | Get all translations for locale |

### Locale APIs

| Method | Path | Auth | Role | Description |
|--------|------|------|------|-------------|
| GET | `/v1/i18n/locales` | No | - | List all supported locales |
| GET | `/v1/i18n/locales/enabled` | No | - | List enabled locales |
| GET | `/v1/i18n/locales/{localeCode}` | No | - | Get locale details |
| GET | `/v1/i18n/locales/default` | No | - | Get default locale |

### Message Bundle APIs

| Method | Path | Auth | Role | Description |
|--------|------|------|------|-------------|
| GET | `/v1/i18n/bundles/{localeCode}` | Yes | USER | Get message bundle |
| GET | `/v1/i18n/bundles/{localeCode}/category/{category}` | Yes | USER | Get bundle by category |
| POST | `/v1/i18n/bundles/{localeCode}/refresh` | Yes | ADMIN | Refresh bundle cache |

## Environment Variables

| Variable | Default | Description | Required |
|----------|---------|-------------|----------|
| SPRING_PROFILES_ACTIVE | dev | Active Spring profile | Yes |
| SERVER_PORT | 8087 | Server port | Yes |
| DB_URL | jdbc:postgresql://localhost:5432/i18n_db | Database URL | Yes |
| DB_USERNAME | admin | Database username | Yes |
| DB_PASSWORD | admin | Database password | Yes |
| REDIS_HOST | localhost | Redis host | Yes |
| REDIS_PORT | 6379 | Redis port | Yes |
| KAFKA_BOOTSTRAP_SERVERS | localhost:9092 | Kafka servers | Yes |
| JWT_SECRET | - | JWT signing secret | Yes |
| I18N_DEFAULT_LOCALE | en | Default locale | Yes |
| I18N_SUPPORTED_LOCALES | en,es,fr,de,hi,ar,zh | Supported locales | Yes |
| I18N_CACHE_TTL | 3600 | Cache TTL (seconds) | No |
| TRANSLATION_API_ENABLED | false | Enable auto-translation | No |
| TRANSLATION_API_PROVIDER | google | Translation provider | No |
| TRANSLATION_API_KEY | - | Translation API key | No |

## Sample Requests

### Translate Single Key

```bash
curl -X POST http://localhost:8087/api/v1/i18n/translate \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "keyName": "welcome.message",
    "localeCode": "es",
    "placeholders": {
      "name": "Juan"
    },
    "fallbackToDefault": true
  }'
```

Response:
```json
{
  "success": true,
  "data": {
    "keyName": "welcome.message",
    "localeCode": "es",
    "translatedText": "Bienvenido, Juan",
    "isAutoTranslated": false,
    "isFallback": false
  },
  "timestamp": "2024-01-01T00:00:00Z"
}
```

### Bulk Translate

```bash
curl -X POST http://localhost:8087/api/v1/i18n/translate/bulk \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "keyNames": ["welcome.message", "goodbye.message", "account.balance"],
    "localeCode": "fr",
    "fallbackToDefault": true
  }'
```

### Get Message Bundle

```bash
curl -X GET http://localhost:8087/api/v1/i18n/bundles/de \
  -H "Authorization: Bearer <token>"
```

Response:
```json
{
  "success": true,
  "data": {
    "localeCode": "de",
    "messages": {
      "welcome.message": "Willkommen",
      "goodbye.message": "Auf Wiedersehen",
      "account.balance": "Kontostand"
    },
    "totalKeys": 150,
    "translatedKeys": 145,
    "missingKeys": 5,
    "completionPercentage": 96.67
  },
  "timestamp": "2024-01-01T00:00:00Z"
}
```

### List Supported Locales

```bash
curl -X GET http://localhost:8087/api/v1/i18n/locales
```

Response:
```json
{
  "success": true,
  "data": [
    {
      "id": "uuid",
      "localeCode": "en",
      "languageName": "English",
      "nativeName": "English",
      "isRtl": false,
      "isEnabled": true,
      "isDefault": true,
      "displayOrder": 1
    },
    {
      "id": "uuid",
      "localeCode": "es",
      "languageName": "Spanish",
      "nativeName": "Español",
      "isRtl": false,
      "isEnabled": true,
      "isDefault": false,
      "displayOrder": 2
    }
  ],
  "timestamp": "2024-01-01T00:00:00Z"
}
```

## Architecture

### Dependencies

**Upstream Services:**
- Identity Service: JWT validation

**Infrastructure:**
- PostgreSQL: Translation data persistence
- Redis: Translation caching and rate limiting
- Kafka: Event publishing

### Integration Flow

1. Client requests translation via REST API
2. Service checks Redis cache for translation
3. If cache miss, retrieves from PostgreSQL
4. Applies placeholder replacement if needed
5. Falls back to default locale if translation missing
6. Caches result in Redis
7. Returns translated text to client

### Kafka Topics

- `banking.i18n.translation-requested`: Published when translation requested
- `banking.i18n.translation-missing`: Published when translation not found
- `banking.i18n.locale-changed`: Published when user changes locale

## Database Schema

### Tables

1. **translation_keys**: Master list of translatable keys
   - id, key_name, category, description, context_hint
   - is_dynamic, placeholder_count
   - created_at, updated_at, deleted_at, version

2. **translations**: Actual translations for each key/locale pair
   - id, translation_key_id, locale_code, translated_text
   - is_auto_translated, quality_score
   - reviewed_by, reviewed_at
   - created_at, updated_at, deleted_at, version

3. **supported_locales**: Configuration of supported locales
   - id, locale_code, language_name, native_name
   - is_rtl, is_enabled, is_default, display_order
   - created_at, updated_at, deleted_at, version

## Caching Strategy

- **Cache Key Format**: `{keyName}_{localeCode}`
- **TTL**: Configurable (default 3600 seconds)
- **Invalidation**: Manual via API or automatic on translation update
- **Cache Warming**: Optional pre-loading of frequently used translations

## Translation Quality

- **Quality Score**: 0-100 scale for translation quality
- **Auto-translated**: Flag indicating machine translation
- **Review Workflow**: Translations can be reviewed and approved
- **Missing Translations**: Tracked and reported for completion

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
docker build -t i18n-service:latest .
```

### Kubernetes/OpenShift

```bash
kubectl apply -f k8s/
```

## Monitoring

- **Health**: `/actuator/health`
- **Metrics**: `/actuator/prometheus`
- **API Docs**: `/swagger-ui.html` (dev only)

## Performance Considerations

1. **Caching**: All translations cached in Redis for fast retrieval
2. **Bulk Operations**: Use bulk translate for multiple keys
3. **Database Indexes**: Optimized indexes on key_name, locale_code
4. **Connection Pooling**: HikariCP with tuned pool sizes
5. **Lazy Loading**: Translations loaded on-demand, not eagerly

## Known Limitations

1. **Auto-translation**: Requires external API key and may incur costs
2. **Context Awareness**: Machine translations may lack context
3. **Placeholder Validation**: No validation of placeholder usage
4. **Translation Memory**: No built-in translation memory system

## Future Improvements

- Implement translation memory for consistency
- Add translation suggestion system
- Implement collaborative translation workflow
- Add support for pluralization rules
- Implement gender-specific translations
- Add translation analytics and usage tracking
- Implement A/B testing for translations
- Add support for regional variants (en-US vs en-GB)

## License

Proprietary - Banking Platform
