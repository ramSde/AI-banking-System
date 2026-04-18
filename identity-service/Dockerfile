# ═══════════════════════════════════════════════════════════════════════
# Identity Service Dockerfile
# Multi-stage build for production-grade deployment
# ═══════════════════════════════════════════════════════════════════════

# ── STAGE 1: Build ─────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-25 AS builder

WORKDIR /build

# Copy parent POM and shared modules first (for layer caching)
COPY pom.xml ./
COPY shared ./shared

# Copy identity-service module
COPY identity-service/pom.xml ./identity-service/
COPY identity-service/src ./identity-service/src

# Build the application (skip tests in Docker build)
RUN mvn clean package -pl identity-service -am -DskipTests

# ── STAGE 2: Runtime ───────────────────────────────────────────────────
FROM eclipse-temurin:25-jre-alpine

# Create non-root user
RUN addgroup -g 1000 banking && \
    adduser -D -u 1000 -G banking banking

# Set working directory
WORKDIR /app

# Copy JAR from builder stage
COPY --from=builder /build/identity-service/target/identity-service-*.jar app.jar

# Change ownership to non-root user
RUN chown -R banking:banking /app

# Switch to non-root user
USER banking:banking

# Expose application and management ports
EXPOSE 8080 8081

# JVM configuration
ENV JAVA_OPTS="-Xms512m \
               -Xmx1024m \
               -XX:+UseG1GC \
               -XX:MaxGCPauseMillis=200 \
               -XX:+HeapDumpOnOutOfMemoryError \
               -XX:HeapDumpPath=/tmp/heapdump.hprof \
               -Djava.security.egd=file:/dev/./urandom \
               -Dfile.encoding=UTF-8"

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8081/actuator/health/liveness || exit 1

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
