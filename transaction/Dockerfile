# Etapa 1: build del proyecto con Gradle
FROM gradle:8.10.2-jdk21 AS builder
WORKDIR /app
COPY . .
RUN gradle clean bootJar --no-daemon

# Etapa 2: imagen liviana para ejecución
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8091
ENTRYPOINT ["sh", "-c", "java -Dserver.port=8091 -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE} -jar app.jar"]

# docker build -t transaction-api .
# docker run -e SPRING_PROFILES_ACTIVE=dev -p 8091:8091 --network devsu-net --name transaction transaction-api


