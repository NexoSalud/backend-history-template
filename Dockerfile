# Dockerfile for Spring Boot microservice
FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

COPY pom.xml .
COPY src ./src
COPY target/*.jar target/

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "target/nexo-history-template-0.0.1-SNAPSHOT.jar"]
