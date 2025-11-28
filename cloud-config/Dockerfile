FROM eclipse-temurin:11-jre
ARG PROJECT_VERSION=0.1.0

RUN mkdir -p /home/app
WORKDIR /home/app

# Copia todos los archivos del servicio
COPY . .

# Agrega el JAR generado por Maven
ADD target/cloud-config-v${PROJECT_VERSION}.jar cloud-config.jar

EXPOSE 9296

ENTRYPOINT ["java", "-jar", "cloud-config.jar"]
