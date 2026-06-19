FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B -q
COPY src ./src
RUN mvn clean package -DskipTests -q

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S agloval && adduser -S agloval -G agloval
COPY --from=build /app/target/*.jar app.jar
USER agloval
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
