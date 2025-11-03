FROM python:3.12-slim

WORKDIR /app

# Instalar dependencias
RUN pip install --no-cache-dir locust requests

# Copiar el script de pruebas
COPY locustfile.py .

# Exponer puerto para la interfaz web de Locust (opcional)
EXPOSE 8089

# Variables de configuración (puedes sobreescribirlas en el Job/Deployment)
ENV LOCUST_USERS=10
ENV LOCUST_SPAWN_RATE=2
ENV LOCUST_RUN_TIME=1m
ENV LOCUST_MODE=headless

# Comando por defecto:
# - En modo headless ejecuta sin interfaz, ideal para pipelines o Jobs
# - En modo web lanza la UI de Locust
CMD if [ "$LOCUST_MODE" = "web" ]; then \
        locust -f locustfile.py; \
    else \
        locust -f locustfile.py --headless \
            --users $LOCUST_USERS \
            --spawn-rate $LOCUST_SPAWN_RATE \
            --run-time $LOCUST_RUN_TIME; \
    fi
