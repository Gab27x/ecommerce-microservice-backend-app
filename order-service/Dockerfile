
FROM openjdk:11
ARG PROJECT_VERSION=0.1.0
WORKDIR /home/app


COPY target/order-service-v${PROJECT_VERSION}.jar order-service.jar

ENV SPRING_PROFILES_ACTIVE dev
EXPOSE 8300

ENTRYPOINT ["java", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}", "-jar", "order-service.jar"]


