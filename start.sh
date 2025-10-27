#!/bin/bash
set -Eeuo pipefail

cleanup_core() {
    echo "Apagando core..."
    docker compose -f core.yml down --remove-orphans || echo "No se pudo apagar core"
}

cleanup_all() {
    echo "Apagando todos los servicios..."
    docker compose -f project-compose.yml down --remove-orphans || echo "No se pudo apagar compose"
}

# --- TRAPS (para limpiar siempre ante error o interrupción) ---
trap cleanup_all ERR
trap cleanup_all INT
trap cleanup_all TERM
trap cleanup_all EXIT

# --- ARRANQUE ---
echo "Prendiendo core..."
docker compose -f core.yml up -d
echo "Core levantado correctamente."

echo "Prendiendo compose..."
docker compose -f project-compose.yml up -d --build
echo "Compose levantado correctamente."

# --- ESPERA INICIAL ---
SLEEP_TIME=30
echo "Esperando ${SLEEP_TIME}s para asegurarse de que los servicios estén listos..."
sleep "$SLEEP_TIME"

# --- CHEQUEO DE EUREKA ---
EUREKA_URL="http://localhost:8761/eureka/apps"
MAX_RETRIES=10
SUCCESS=false
NODES_NUM=5

for i in $(seq 1 "$MAX_RETRIES"); do
    echo "Intento $i de $MAX_RETRIES..."
    RESPONSE=$(curl -s --max-time 5 "$EUREKA_URL" || true)

    if [ -n "$RESPONSE" ]; then
        APP_COUNT=$(echo "$RESPONSE" | grep -o "<application>" | wc -l | tr -d ' ')
        UP_COUNT=$(echo "$RESPONSE" | grep -c "<status>UP</status>" | tr -d ' ')
        echo "Aplicaciones registradas: $APP_COUNT, UP: $UP_COUNT"

        if [ "$APP_COUNT" -eq "$NODES_NUM" ] && [ "$UP_COUNT" -eq "$NODES_NUM" ]; then
            SUCCESS=true
            break
        fi
    else
        echo "No se obtuvo respuesta de Eureka."
    fi

    sleep 15
done

# --- RESULTADO ---
if [ "$SUCCESS" = true ]; then
    echo "Eureka está completamente operativo. Todo bien."
else
    echo "Eureka no alcanzó las $NODES_NUM aplicaciones UP tras $MAX_RETRIES intentos."
    exit 1
fi

# --- DESACTIVAR CLEANUP SI TODO SALIÓ BIEN ---
trap - ERR INT TERM EXIT
echo "Script finalizado correctamente. Los contenedores permanecen arriba."
