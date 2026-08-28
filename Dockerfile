# syntax=docker/dockerfile:1

##### builder #####################################################################
FROM eclipse-temurin:25-jdk-alpine AS builder

WORKDIR /app

COPY mvnw pom.xml ./
COPY .mvn .mvn

RUN ./mvnw dependency:go-offline -q

COPY src ./src
RUN ./mvnw clean package -DskipTests -q

RUN java -Djarmode=layertools -jar target/*.jar extract --destination target/extracted

##### runtime #####################################################################
FROM eclipse-temurin:25-jre-jammy AS runtime

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# Debian/Ubuntu equivalent of Alpine's "addgroup -S / adduser -S -G":
# groupadd/useradd -r create a system group/user, matching the same intent.
RUN groupadd -r spring && useradd -r -g spring spring

WORKDIR /app

# Copy layered JAR contents (ordered by change frequency for best caching)
COPY --from=builder --chown=spring:spring /app/target/extracted/dependencies/ ./
COPY --from=builder --chown=spring:spring /app/target/extracted/spring-boot-loader/ ./
COPY --from=builder --chown=spring:spring /app/target/extracted/snapshot-dependencies/ ./
COPY --from=builder --chown=spring:spring /app/target/extracted/application/ ./

USER spring

EXPOSE 9001

HEALTHCHECK --interval=10s --timeout=5s --start-period=30s --retries=5 \
  CMD curl -f http://localhost:9001/actuator/health || exit 1

# exec form via sh -c so $JAVA_OPTS still expands, but `exec` replaces the
# shell as PID 1 so SIGTERM reaches the JVM directly for graceful shutdown
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]