# Multi-stage build using Java 21 for root repository context
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Cache dependencies
COPY backend/pom.xml ./
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY backend/src ./src
RUN mvn clean package -DskipTests

# Runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/arcade-backend-*.jar app.jar

EXPOSE 8080
ENV PORT=8080

ENTRYPOINT ["java", "-jar", "app.jar"]
