#!/usr/bin/env bash
# RN-01: envia N solicitudes SIMULTANEAS para la misma sala y horario. Solo una debe responder 201.
# Uso: scripts/prueba-concurrencia.sh [baseUrl] [N]     (requiere bash, curl y python)
BASE=${1:-http://localhost:8080}
N=${2:-6}
TOKEN=$(curl -s -H 'Content-Type: application/json' -X POST "$BASE/auth/login" \
  -d '{"usuario":"recepcion","contrasena":"recep123"}' | python -c "import sys,json;print(json.load(sys.stdin)['token'])")
DIA=$(python -c "import datetime,random;print(datetime.date.today()+datetime.timedelta(days=random.randint(400,900)))")
TMP=$(mktemp -d)
for i in $(seq 1 "$N"); do
  MIEMBRO=$(( (i % 2) + 1 ))   # alterna Ana y Luis
  curl -s -o "$TMP/r$i.json" -w "%{http_code}\n" -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
    -X POST "$BASE/api/reservas" \
    -d "{\"miembroId\":$MIEMBRO,\"salaId\":6,\"inicio\":\"${DIA}T10:00:00\",\"fin\":\"${DIA}T12:00:00\",\"asistentes\":2}" \
    > "$TMP/c$i" &
done
wait
OK=$(cat "$TMP"/c* | grep -c '^201$'); CONFLICTO=$(cat "$TMP"/c* | grep -c '^409$')
echo "Dia $DIA - respuestas: $(cat "$TMP"/c* | sort | uniq -c | tr '\n' ' ')"
cat "$TMP"/r*.json | grep -o '"codigo":"[^"]*"' | sort | uniq -c
rm -rf "$TMP"
if [ "$OK" -eq 1 ] && [ "$CONFLICTO" -eq $((N-1)) ]; then echo "OK: una 201 y $CONFLICTO 409 (RN-01)"; else echo "FALLO"; exit 1; fi
