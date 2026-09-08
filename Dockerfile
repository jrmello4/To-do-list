# ==============================================================================
# Stage 1: Build the application with Maven and Eclipse Temurin 17 JDK
# ==============================================================================
FROM maven:3.9.9-eclipse-temurin-17-alpine AS builder

WORKDIR /build

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy sources and package the application
COPY src ./src
RUN mvn clean package -DskipTests -B

# ==============================================================================
# Stage 2: Minimal, secure runtime container with Eclipse Temurin 17 JRE
# ==============================================================================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Run as non-root user for enhanced security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=builder /build/target/to-do-list-1.0.0.jar /app/app.jar

RUN chown -R appuser:appgroup /app
USER appuser

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD wget -qO- http://localhost:8080/ || exit 1

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/app.jar"]
