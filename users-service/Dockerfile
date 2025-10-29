# syntax=docker/dockerfile:1

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY . .
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -DskipTests -pl users-service -am package org.springframework.boot:spring-boot-maven-plugin:repackage

FROM eclipse-temurin:21-jre
ENV JAVA_OPTS=""
COPY --from=build /src/users-service/target/*.jar /app.jar
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -Dspring.profiles.active=docker -jar /app.jar"]