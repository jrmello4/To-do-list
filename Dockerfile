# ---------- build ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# As dependências vêm antes do código: enquanto o pom.xml não mudar, esta
# camada é reaproveitada e o build não baixa tudo de novo a cada alteração.
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B package -DskipTests

# ---------- execução ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

# Usuário sem privilégios: um processo comprometido não vira root no container.
RUN groupadd --system spring && useradd --system --gid spring spring
USER spring:spring

COPY --from=build /app/target/to-do-list-*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
