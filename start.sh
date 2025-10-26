#!/bin/bash
set -Eeuo pipefail

cleanup_core() {
    echo "Apagando core..."
    docker compose -f core.yml down --remove-orphans || echo "No se pudo apagar core"
}

cleanup_all() {
    echo "Apagando todos los servicios..."
    docker compose -f compose.yml down --remove-orphans || echo "No se pudo apagar compose"
    cleanup_core
}

# --- TRAPS (para limpiar SIEMPRE ante error o interrupción) ---
trap cleanup_all ERR
trap cleanup_all INT
trap cleanup_all TERM
trap cleanup_all EXIT

# --- ARRANQUE ---
echo "Prendiendo core..."
docker compose -f core.yml up -d
echo "Core levantado correctamente."

echo "Prendiendo compose..."
docker compose -f compose.yml up -d --build
echo "Compose levantado correctamente."

echo "Esperando 45s para asegurarse de que los servicios estén listos..."
sleep 45

EUREKA_URL="http://localhost:8761/eureka/apps"
MAX_RETRIES=10
SUCCESS=false

for i in $(seq 1 $MAX_RETRIES); do
    echo "Intento $i de $MAX_RETRIES..."
    RESPONSE=$(curl -s --max-time 5 "$EUREKA_URL" || true)

    if [ -n "$RESPONSE" ]; then
        APP_COUNT=$(echo "$RESPONSE" | grep -o "<application>" | wc -l)
        UP_COUNT=$(echo "$RESPONSE" | grep -c "<status>UP</status>")
        echo "Aplicaciones registradas: $APP_COUNT, UP: $UP_COUNT"

        if [ "$APP_COUNT" -eq 9 ] && [ "$UP_COUNT" -eq 9 ]; then
            SUCCESS=true
            break
        fi
    else
        echo "No se obtuvo respuesta de Eureka."
    fi
    sleep 15
done

if [ "$SUCCESS" = true ]; then
    echo "Eureka está completamente operativo. Todo bien."
else
    echo "Eureka no alcanzó las 9 aplicaciones UP tras $MAX_RETRIES intentos."
    exit 1
fi

# --- DESACTIVAR CLEANUP SI TODO SALIÓ BIEN ---
trap - ERR INT TERM EXIT
echo "Script finalizado correctamente. Contenedores permanecen arriba."
