FROM eclipse-temurin:11-jre
ARG PROJECT_VERSION=0.1.0

RUN mkdir -p /home/app
WORKDIR /home/app

ENV SPRING_PROFILES_ACTIVE dev

# Copia todo desde el contexto ./service-discovery
COPY . .

# Agrega el JAR generado por Maven
ADD target/service-discovery-v${PROJECT_VERSION}.jar service-discovery.jar

EXPOSE 8761

ENTRYPOINT ["java", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}", "-jar", "service-discovery.jar"]
