# Multi-stage Dockerfile for Device Intelligence Service
# Stage 1: Build
FROM maven:3.9-eclipse-temurin-25 AS builder

WORKDIR /build

# Copy pom files
COPY pom.xml .
COPY device-service/pom.xml ./device-service/

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY device-service/src ./device-service/src

# Build application
RUN mvn clean package -DskipTests -f device-service/pom.xml

# Stage 2: Runtime
FROM eclipse-temurin:25-jre-alpine

# Create non-root user
RUN addgroup -g 1000 appgroup && \
    adduser -D -u 1000 -G appgroup appuser

WORKDIR /app

# Copy JAR from builder
COPY --from=builder /build/device-service/target/*.jar app.jar

# Change ownership
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8084

# JVM Configuration
ENV JAVA_OPTS="-Xms512m \
               -Xmx1024m \
               -XX:+UseG1GC \
               -XX:MaxGCPauseMillis=200 \
               -XX:+HeapDumpOnOutOfMemoryError \
               -XX:HeapDumpPath=/tmp/heapdump.hprof \
               -Djava.security.egd=file:/dev/./urandom"

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8084/api/actuator/health || exit 1

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
