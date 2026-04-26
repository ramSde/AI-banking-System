# Multi-stage Dockerfile for API Gateway
# Stage 1: Build stage using Maven
# Stage 2: Runtime stage using JRE

# ============================================
# Stage 1: Build
# ============================================
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

# Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests -B

# ============================================
# Stage 2: Runtime
# ============================================
FROM eclipse-temurin:17-jre-alpine

# Create non-root user for security
RUN addgroup -g 1000 banking && \
    adduser -D -u 1000 -G banking banking

WORKDIR /app

# Copy JAR from builder stage
COPY --from=builder /build/target/api-gateway.jar app.jar

# Change ownership to non-root user
RUN chown -R banking:banking /app

# Create logs directory
RUN mkdir -p /app/logs && chown -R banking:banking /app/logs

# Switch to non-root user
USER banking:banking

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# JVM configuration
ENV JAVA_OPTS="-Xms512m \
    -Xmx1024m \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=200 \
    -XX:+HeapDumpOnOutOfMemoryError \
    -XX:HeapDumpPath=/app/logs/heap-dump.hprof \
    -Djava.security.egd=file:/dev/./urandom \
    -Dfile.encoding=UTF-8 \
    -Duser.timezone=UTC"

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
