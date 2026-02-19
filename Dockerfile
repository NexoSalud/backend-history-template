# Dockerfile for Spring Boot microservice
FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

COPY pom.xml .
# Consider running dependency:go-offline for faster builds
# RUN mvn dependency:go-offline

COPY src ./src

# Ensure you have maven in your base image or use a multi-stage build
# RUN mvn clean package -DskipTests

EXPOSE 8080

# The JAR name should match the artifactId in pom.xml
ENTRYPOINT ["java", "-jar", "target/nexo-history-template-0.0.1-SNAPSHOT.jar"]
