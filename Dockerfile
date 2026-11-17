# =====================================================================
# Geladeira Solidaria Inteligente - API Java (Spring Boot)
# Global Solution 2026/2 - CitizenTech - DevOps Tools & Cloud Computing
# =====================================================================
# Build em dois estagios:
#   1) "build": compila o projeto com Maven e gera o .jar
#   2) "runtime": imagem final, so com o Java (JRE) e o .jar,
#      rodando com um usuario comum (nao-root)
# =====================================================================

# ---------- Estagio 1: compilacao ----------
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /build

COPY api/pom.xml .
COPY api/src ./src

# Gera o .jar (os testes ja rodam no projeto Java, por isso -DskipTests).
# O cache em /root/.m2 guarda as dependencias baixadas entre um build e outro,
# entao o segundo build e bem mais rapido.
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q -DskipTests package \
 && cp "$(find target -maxdepth 1 -name '*.jar' ! -name '*-plain.jar' | head -n 1)" app.jar


# ---------- Estagio 2: execucao ----------
FROM eclipse-temurin:21-jre-alpine AS runtime

LABEL org.opencontainers.image.title="geladeira-solidaria-api" \
      org.opencontainers.image.description="API da Geladeira Solidaria Inteligente (FIAP GS 2026/2 - CitizenTech)"

# Variaveis de ambiente da aplicacao.
# Os dados do banco e o segredo do JWT NAO ficam aqui: vem do docker-compose (.env).
ENV TZ=America/Sao_Paulo \
    SERVER_PORT=8080 \
    JAVA_OPTS="-XX:MaxRAMPercentage=60 -XX:+UseSerialGC"

# Usuario e grupo sem privilegios para rodar a aplicacao
RUN addgroup -S geladeira && adduser -S -G geladeira -h /app -s /sbin/nologin appuser

# Diretorio de trabalho da aplicacao
WORKDIR /app

# Copia o .jar do estagio de build ja pertencendo ao usuario da aplicacao
COPY --from=build --chown=appuser:geladeira /build/app.jar /app/app.jar

# Pasta de logs da aplicacao (gravavel pelo appuser)
RUN mkdir -p /app/logs && chown appuser:geladeira /app/logs

# A partir daqui, tudo roda como appuser (nao-root)
USER appuser

# Porta em que a API escuta dentro do container
EXPOSE 8080

# O Docker consulta o Actuator para saber se a API esta saudavel
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health | grep -q '"UP"' || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
