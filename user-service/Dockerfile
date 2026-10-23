FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app


COPY pom.xml .

# Maven dependencies
RUN --mount=type=cache,target=/root/.m2/repository \
    mvn -B dependency:go-offline

COPY src ./src

# Maven cache
RUN --mount=type=cache,target=/root/.m2/repository \
    mvn -B -DskipTests package

# Launch
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]