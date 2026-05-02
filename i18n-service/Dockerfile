# Stage 1: Build
FROM maven:3.9-eclipse-temurin-25 AS builder

WORKDIR /build

# Copy parent pom and shared modules first for better caching
COPY pom.xml ./
COPY shared/ ./shared/

# Copy service pom
COPY i18n-service/pom.xml ./i18n-service/

# Download dependencies
RUN mvn dependency:go-offline -B -f i18n-service/pom.xml

# Copy source code
COPY i18n-service/src ./i18n-service/src

# Build the application
RUN mvn clean package -DskipTests -f i18n-service/pom.xml

# Stage 2: Runtime
FROM eclipse-temurin:25-jre-alpine

# Create non-root user
RUN addgroup -g 1000 appgroup && \
    adduser -D -u 1000 -G appgroup appuser

WORKDIR /app

# Copy JAR from builder
COPY --from=builder /build/i18n-service/target/*.jar app.jar

# Change ownership
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8017

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8017/api/actuator/health || exit 1

# JVM options
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp -Djava.security.egd=file:/dev/./urandom"

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
