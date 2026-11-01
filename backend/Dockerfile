# syntax=docker/dockerfile:1.7

FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew dependencies --no-daemon

COPY src ./src
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew bootJar --no-daemon && \
    find build/libs -type f -name '*.jar' ! -name '*-plain.jar' \
        -exec cp '{}' /app/app.jar \;

FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

COPY --from=build --chmod=0444 /app/app.jar ./app.jar

USER 10001:10001

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
