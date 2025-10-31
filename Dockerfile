# Imagen base con Node y Newman
FROM node:18-alpine

# Instalar newman globalmente
RUN npm install -g newman

# Crear el directorio de trabajo
WORKDIR /app

# Copiar los archivos necesarios
COPY postman_collection.json .
COPY variables_pipeline_minikube.json .

# Comando por defecto: ejecutar colección con entorno
CMD ["newman", "run", "postman_collection.json", "-e", "variables_pipeline_minikube.json", "--reporters", "cli"]
