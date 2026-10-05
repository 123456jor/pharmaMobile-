# Compara el timeout de curl con el timeout configurado por la aplicación.
# En AppModule.kt activa MODO_TIMEOUT_PARA_PRUEBAS=true (1 ms) para verificar ErrorApi.TiempoAgotado.
$baseUrl = 'http://localhost:8080/api/v1'
curl.exe --max-time 0.001 -sS -o NUL -w "HTTP %{http_code}`n" "$baseUrl/productos?pagina=0&tamanio=20"
# curl informa HTTP 000 si vence antes de recibir una respuesta; registra el resultado real.
