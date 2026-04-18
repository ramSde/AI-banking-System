# ══════════════════════════════════════════════════════════════════════
# API Gateway — Multi-stage Dockerfile
# Production-grade Docker image with security best practices
# ══════════════════════════════════════════════════════════════════════

# ── BUILD STAGE ───────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-25 AS builder

# Set working directory
WORKDIR /app

# Copy parent pom first for better layer caching
COPY ../pom.xml ./pom.xml

# Copy module pom
COPY pom.xml ./api-gateway/pom.xml

# Copy source code
COPY src ./api-gateway/src

# Build the application
WORKDIR /app/api-gateway
RUN mvn clean package -DskipTests -Dmaven.javadoc.skip=true

# ── RUNTIME STAGE ─────────────────────────────────────────────────────
FROM eclipse-temurin:25-jre-alpine AS runtime

# Install curl for health checks
RUN apk add --no-cache curl

# Create non-root user for security
RUN addgroup -g 1000 banking && \
    adduser -D -s /bin/sh -u 1000 -G banking banking

# Set working directory
WORKDIR /app

# Copy JAR from build stage
COPY --from=builder /app/api-gateway/target/*.jar app.jar

# Change ownership to non-root user
RUN chown -R banking:banking /app

# Switch to non-root user
USER banking

# Expose ports
EXPOSE 8080 8081

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8081/actuator/health || exit 1

# JVM configuration for production
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp -Djava.security.egd=file:/dev/./urandom"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]