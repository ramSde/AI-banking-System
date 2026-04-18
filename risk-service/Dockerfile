# ============================================
# Multi-stage Dockerfile for Risk Service
# ============================================

# Stage 1: Build
FROM maven:3.9-eclipse-temurin-25 AS builder

WORKDIR /build

# Copy parent POM and module POM
COPY pom.xml ./
COPY risk-service/pom.xml ./risk-service/

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -B -pl risk-service

# Copy source code
COPY risk-service/src ./risk-service/src

# Build the application
RUN mvn clean package -DskipTests -pl risk-service

# Stage 2: Runtime
FROM eclipse-temurin:25-jre-alpine

# Create non-root user
RUN addgroup -g 1000 appgroup && \
    adduser -u 1000 -G appgroup -s /bin/sh -D appuser

WORKDIR /app

# Copy JAR from builder stage
COPY --from=builder /build/risk-service/target/*.jar app.jar

# Create directories
RUN mkdir -p /var/log/risk-service && \
    chown -R appuser:appgroup /app /var/log/risk-service

# Switch to non-root user
USER appuser:appgroup

# Expose port
EXPOSE 8083

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8083/api/actuator/health || exit 1

# JVM options
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp -Djava.security.egd=file:/dev/./urandom"

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
