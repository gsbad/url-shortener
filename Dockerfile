# Build multi-stage: a imagem final não carrega Maven nem código-fonte.
# Tags fixas nos dois estágios — build precisa ser reproduzível.

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Dependências antes do código: enquanto o pom.xml não mudar, o Docker
# reaproveita a camada e não baixa o repositório Maven inteiro de novo.
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src/ ./src/
RUN mvn -B clean package -DskipTests

# ---

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Usuário sem privilégio. A aplicação não precisa de root.
RUN addgroup -S app && adduser -S app -G app

COPY --from=build /build/target/*.jar app.jar
RUN chown app:app app.jar
USER app

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
    CMD wget -q --spider http://localhost:8080/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
