FROM openjdk:11
ARG PROJECT_VERSION=0.1.0
WORKDIR /home/app

# Copiamos directamente el JAR compilado
COPY target/user-service-v${PROJECT_VERSION}.jar user-service.jar

ENV SPRING_PROFILES_ACTIVE=dev
EXPOSE 8700

ENTRYPOINT ["java", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}", "-jar", "user-service.jar"]
