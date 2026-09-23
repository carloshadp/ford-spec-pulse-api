# =============================================================
#  Ford SpecPulse API - imagem de producao (Render / Docker)
#  Build multi-stage: compila com Maven + JDK 21, roda em JRE 21
# =============================================================

# ---------- Estagio 1: build ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Copia so o pom primeiro para aproveitar o cache de dependencias:
# se o pom nao mudar, o Render reusa esta camada e o build fica bem mais rapido.
COPY pom.xml .
RUN mvn -B -ntp dependency:go-offline

COPY src ./src
RUN mvn -B -ntp -DskipTests package

# ---------- Estagio 2: runtime ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Usuario sem privilegios (o H2 grava em /app/data, entao precisa ser dono da pasta)
RUN groupadd -r specpulse && useradd -r -g specpulse specpulse \
    && mkdir -p /app/data

COPY --from=build /build/target/*.jar /app/app.jar
RUN chown -R specpulse:specpulse /app
USER specpulse

# Plano free do Render = 512 MB; limita o heap para a JVM nao ser morta por OOM
ENV JAVA_OPTS="-XX:MaxRAMPercentage=70 -XX:+UseSerialGC"

# O Render injeta a variavel PORT; application.properties usa ${PORT:8080}
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
