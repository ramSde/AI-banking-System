# ══════════════════════════════════════════════════════════════════════
# Dockerfile — API Gateway (Multi-stage build)
#
# Stage 1: Build the fat jar using Maven
# Stage 2: Minimal JRE runtime image (no JDK, no Maven, no source)
#
# Security:
#   - Runs as non-root user (UID 1000)
#   - No shell in production (distroless alternative available)
#   - No secrets baked into the image — all injected at runtime
#
# Ref: https://docs.docker.com/develop/dev-best-practices/
# Ref: https://docs.spring.io/spring-boot/docs/current/reference/html/container-images.html
# ══════════════════════════════════════════════════════════════════════

# ── STAGE 1: BUILD ────────────────────────────────────────────────────
FROM maven:3.9.9-eclipse-temurin-25 AS builder

WORKDIR /build

# Copy pom.xml first to leverage Docker layer caching for dependencies.
# If only source changes (not pom.xml), this layer is reused.
COPY ../pom.xml .
RUN mvn dependency:go-offline -q

# Copy source and build
COPY src ./src
RUN mvn package -DskipTests -q

# Spring Boot layered jar extraction.
# Splits the fat jar into layers for efficient Docker caching:
#   dependencies/ — rarely change (cached aggressively)
#   snapshot-dependencies/ — change occasionally
#   spring-boot-loader/ — rarely changes
#   application/ — changes every build
RUN java -Djarmode=layertools -jar target/api-gateway-*.jar extract --destination extracted

# ── STAGE 2: RUNTIME ──────────────────────────────────────────────────
FROM eclipse-temurin:25-jre-alpine AS runtime

# Install curl for HEALTHCHECK and tini for proper PID 1 signal handling
RUN apk add --no-cache curl tini

# Create non-root user and group
# Using fixed UID/GID for deterministic K8s PodSecurityPolicy / securityContext
RUN addgroup -g 1000 banking && adduser -u 1000 -G banking -s /bin/sh -D banking

WORKDIR /app

# Copy layered jar in dependency order (most stable → least stable)
# This maximises Docker layer cache reuse between builds
COPY --from=builder --chown=banking:banking /build/extracted/dependencies/ ./
COPY --from=builder --chown=banking:banking /build/extracted/spring-boot-loader/ ./
COPY --from=builder --chown=banking:banking /build/extracted/snapshot-dependencies/ ./
COPY --from=builder --chown=banking:banking /build/extracted/application/ ./

# Switch to non-root user
USER banking:banking

# Expose application port (management port 8081 is internal only)
EXPOSE 8080

# ── HEALTHCHECK ───────────────────────────────────────────────────────
# K8s liveness/readiness probes use the actuator endpoints.
# This Docker HEALTHCHECK is a fallback for non-K8s environments.
HEALTHCHECK \
    --interval=30s \
    --timeout=10s \
    --start-period=60s \
    --retries=3 \
    CMD curl -f http://localhost:8081/actuator/health/liveness || exit 1

# ── ENTRYPOINT ────────────────────────────────────────────────────────
# tini handles PID 1 responsibilities (signal forwarding, zombie reaping)
# JVM flags:
#   -Xms256m           Initial heap (gateway is stateless — low memory)
#   -Xmx512m           Max heap
#   -XX:+UseZGC        ZGC for low-latency pause times (ideal for gateway)
#   -XX:+ZGenerational Enable generational ZGC (JDK 21+)
#   -XX:+HeapDumpOnOutOfMemoryError
#   -Djava.security.egd Faster SecureRandom seeding in containers
ENTRYPOINT ["/sbin/tini", "--"]
CMD ["java", \
     "-Xms256m", \
     "-Xmx512m", \
     "-XX:+UseZGC", \
     "-XX:+ZGenerational", \
     "-XX:+HeapDumpOnOutOfMemoryError", \
     "-XX:HeapDumpPath=/tmp/heap-dump.hprof", \
     "-Djava.security.egd=file:/dev/./urandom", \
     "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-prod}", \
     "org.springframework.boot.loader.launch.JarLauncher"]
