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

# curl entra só para o HEALTHCHECK: a imagem JRE não traz cliente HTTP nenhum,
# e sem um deles a verificação abaixo não teria como perguntar nada. É a única
# coisa instalada aqui, sem recomendados, e a lista do apt sai junto.
RUN apt-get update \
    && apt-get install --yes --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# Usuário sem privilégios: um processo comprometido não vira root no container.
RUN groupadd --system spring && useradd --system --gid spring spring
USER spring:spring

COPY --from=build /app/target/to-do-list-*.jar app.jar

EXPOSE 8080

# Quem orquestra precisa saber se a aplicação responde, e não só se o processo
# existe: um processo que subiu mas não conectou no banco continua "rodando"
# para o Docker enquanto devolve erro para quem chama. /actuator/health é
# público justamente para este uso.
#
# start-period generoso porque o Flyway roda antes de a porta abrir, e uma
# migration numa base grande não é instantânea. A porta vem de PORT para a
# verificação continuar valendo onde a hospedagem escolhe a porta.
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl --fail --silent "http://127.0.0.1:${PORT:-8080}/actuator/health" || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
