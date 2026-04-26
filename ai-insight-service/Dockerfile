FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="banking-platform"
LABEL service="ai-insight-service"

WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY target/ai-insight-service-1.0.0.jar app.jar

RUN chown -R appuser:appgroup /app

USER appuser

EXPOSE 8080

ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
