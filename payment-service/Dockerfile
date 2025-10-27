
FROM openjdk:11
ARG PROJECT_VERSION=0.1.0
WORKDIR /home/app


COPY target/payment-service-v${PROJECT_VERSION}.jar payment-service.jar

ENV SPRING_PROFILES_ACTIVE dev
EXPOSE 8400

ENTRYPOINT ["java", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}", "-jar", "payment-service.jar"]


