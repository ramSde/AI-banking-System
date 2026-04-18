# ══════════════════════════════════════════════════════════════════════
# API Gateway — Multi-stage Dockerfile
# Production-grade container build for banking platform API Gateway
# ══════════════════════════════════════════════════════════════════════

# ── STAGE 1: Build Stage ──────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-25 AS builder

# Set working directory
WORKDIR /app

# Copy parent pom first for better layer caching
COPY pom.xml .
COPY api-gateway/pom.xml ./api-gateway/
COPY shared/ ./shared/

# Download dependencies (cached layer if pom.xml unchanged)
RUN mvn dependency:go-offline -B -f api-gateway/pom.xml

# Copy source code
COPY api-gateway/src ./api-gateway/src

# Build the application
RUN mvn clean package -B -f api-gateway/pom.xml -DskipTests

# ── STAGE 2: Runtime Stage ────────────────────────────────────────────
FROM eclipse-temurin:25-jre-alpine

# Install curl for health checks and debugging
RUN apk add --no-cache curl

# Create non-root user for security
RUN addgroup -g 1000 banking && \
    adduser -D -s /bin/sh -u 1000 -G banking banking

# Set working directory
WORKDIR /app

# Copy JAR from build stage
COPY --from=builder /app/api-gateway/target/api-gateway-*.jar app.jar

# Change ownership to non-root user
RUN chown -R banking:banking /app

# Switch to non-root user
USER banking

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# JVM configuration for production
ENV JAVA_OPTS="-Xms512m -Xmx1024m \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=200 \
    -XX:+HeapDumpOnOutOfMemoryError \
    -XX:HeapDumpPath=/tmp \
    -Djava.security.egd=file:/dev/./urandom \
    -Dspring.profiles.active=prod"

# Entry point
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# Metadata
LABEL maintainer="Banking Platform Team" \
      version="1.0.0" \
      description="Production-grade API Gateway for Banking Platform" \
      org.opencontainers.image.source="https://github.com/banking-platform/api-gateway" \
      org.opencontainers.image.documentation="https://docs.banking-platform.com/api-gateway" \
      org.opencontainers.image.vendor="Banking Platform" \
      org.opencontainers.image.licenses="Proprietary"