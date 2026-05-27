# Dockerfile for local testing (ARM Pi)
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY target/*.jar .
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "nexo-history-template-0.0.1-SNAPSHOT.jar"]
