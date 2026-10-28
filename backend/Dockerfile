FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /workspace
COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY domain domain
COPY application application
COPY infrastructure infrastructure
RUN chmod +x mvnw && ./mvnw -B -DskipTests package

FROM eclipse-temurin:21-jre-jammy
RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --system --uid 10001 --create-home soluciones
WORKDIR /app
COPY --from=build /workspace/infrastructure/target/soluciones.jar app.jar
USER soluciones
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
