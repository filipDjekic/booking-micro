FROM eclipse-temurin:21-jre
ARG JAR=target/*.jar
COPY ${JAR} app.jar
ENV JAVA_OPTS=""
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -Dspring.profiles.active=docker -jar /app.jar"]