# Antigüedad y migración

Se agregó la tabla Sincronizacion (una fila, id=1) y la migración 1.sqm de versión 1 a 2. La instalación con -r conservó exactamente los nueve productos de la base anterior. Ver consultas/migracion-verificada.json. No se borraron datos para comprobar la migración.

ProductoRepositorioOffline confirma lista y fecha en una transacción únicamente tras enviar todos los pendientes y descargar todas las páginas. Un fallo conserva la fecha anterior. Se inyecta el reloj para probar sin esperar. ProductoScreen observa la fecha y actualiza el texto cada minuto sin llamar a HTTP.

Pruebas adicionales: nulo, segundos, reloj atrasado, minutos, horas, días y éxito/fallo de sincronización. La primera ejecución detectó un error en el JSON del nuevo test: tamano en lugar de tamanio. Se corrigió la respuesta de prueba para respetar el contrato real, sin cambiar ni debilitar las aserciones. La repetición completa pasó: 88 pruebas, cero fallos. Salida inicial y corregida conservadas.

La captura esc5_antes_login_sin_red.png documenta el comportamiento anterior: login en modo avión, pese a existir nueve productos en SQLite. La expiración de ese token anterior no se midió; no se presenta como la prueba controlada definitiva de expiración. Se repetirá con un JWT realmente emitido y vencido.
