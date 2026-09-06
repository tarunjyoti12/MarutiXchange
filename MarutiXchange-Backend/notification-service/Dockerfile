# notification-service Dockerfile
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn clean package -DskipTests -q

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
COPY --from=builder /app/target/notification-service-1.0.0.jar app.jar
RUN chown appuser:appgroup app.jar
USER appuser
EXPOSE 8051
ENTRYPOINT ["java", "-jar", "app.jar"]
