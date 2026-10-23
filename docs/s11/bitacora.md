# Bitácora de cinco escenarios

Fechas en America/Lima. El emulador está en GMT y su barra usa reloj de 12 horas; no se alteró el reloj. Las consultas contienen filas inactivas que la pantalla filtra.

## E1. Apertura sin red con datos previos

**1. Identificador y nombre:** E1. Apertura sin red con datos previos

**2. Fecha y hora real:** 2026-10-23T14:15:20.356694-05:00

**3. Plataforma:** Android - Pixel 8 Pro, API 37.1

**4. Pasos exactos seguidos:** Con red: iniciar sesión como admin y abrir Productos; sincronizar. Forzar cierre, activar modo avión y desactivar wifi y datos. Reabrir y entrar a Productos.

**5. Qué mostró la aplicación:** Inventario visible, cinco productos activos y aviso «Sin conexión: mostrando datos guardados».

**6. Qué había en la base local:** 9 filas totales, 5 activas, 0 pendientes. Marca de sincronización: [{'id': 1, 'ultimaMillis': 1792782864862}]. Consulta: SELECT id,nombre,precio,stock,pendiente,estado,categoriaId FROM Producto ORDER BY nombre. Archivo: consultas/esc1.json.

**7. Captura:** capturas/esc1_listado_sin_red.png

**8. Conclusión en una línea:** Cerrar el proceso no elimina la copia local: el catálogo sigue disponible sin volver a descargarlo.

## E2. Registro sin red

**1. Identificador y nombre:** E2. Registro sin red

**2. Fecha y hora real:** 2026-10-23T14:16:46.751154-05:00

**3. Plataforma:** Android - Pixel 8 Pro, API 37.1

**4. Pasos exactos seguidos:** Mantener modo avión. Registrar AA_Autonoma_Jorge_23Oct, precio 8.50, stock 17, categoría 1. Cerrar teclado y desplazar el inventario al inicio para ver el nuevo registro.

**5. Qué mostró la aplicación:** Seis productos activos; el nuevo aparece con «Pendiente de sincronizar · ID -1». Se intentó reconectar antes de tomar la captura final, pero la consulta comprobó que no hubo envío; se volvió a modo avión para documentar el pendiente.

**6. Qué había en la base local:** 10 filas totales, 6 activas, 1 pendientes. Producto observado: id=-1, pendiente=1, precio=8.5, stock=17. Marca de sincronización: [{'id': 1, 'ultimaMillis': 1792782864862}]. Consulta: SELECT id,nombre,precio,stock,pendiente,estado,categoriaId FROM Producto ORDER BY nombre. Archivo: consultas/esc2_final.json.

**7. Captura:** capturas/esc2_pendiente_visible.png

**8. Conclusión en una línea:** El registro se conserva con ID temporal negativo aunque el servidor no pueda recibirlo.

## E3. Reconexión y envío

**1. Identificador y nombre:** E3. Reconexión y envío

**2. Fecha y hora real:** 2026-10-23T14:17:43.083076-05:00

**3. Plataforma:** Android - Pixel 8 Pro, API 37.1

**4. Pasos exactos seguidos:** Desactivar modo avión, habilitar wifi y datos, esperar conexión y pulsar Sincronizar. En Postman: login de la colección S11 Autonoma Jorge, después GET /api/v1/productos/83.

**5. Qué mostró la aplicación:** Desaparece la marca pendiente; el producto queda con ID 83. Postman responde 200 OK y devuelve nombre AA_Autonoma_Jorge_23Oct, precio 8.5 y stock 17.

**6. Qué había en la base local:** 10 filas totales, 6 activas, 0 pendientes. Producto observado: id=83, pendiente=0, precio=8.5, stock=17. Marca de sincronización: [{'id': 1, 'ultimaMillis': 1792783061571}]. Consulta: SELECT id,nombre,precio,stock,pendiente,estado,categoriaId FROM Producto ORDER BY nombre. Archivo: consultas/esc3.json.

**7. Captura:** capturas/esc3_producto_sincronizado.png

**8. Conclusión en una línea:** El GET independiente de Postman confirma que el producto llegó a PharmaSoft y recibió el ID 83.

## E4. Primera apertura sin red

**1. Identificador y nombre:** E4. Primera apertura sin red

**2. Fecha y hora real:** 2026-10-23T14:19:04.658576-05:00

**3. Plataforma:** Android - Pixel 8 Pro, API 37.1

**4. Pasos exactos seguidos:** Confirmar cero pendientes y respaldar SQLite. Forzar cierre, activar modo avión, ejecutar pm clear pe.edu.upeu.pharmamobil y abrir la app.

**5. Qué mostró la aplicación:** «Todavía no hay productos», explicación para conectarse e iniciar sesión y «Aún no se ha sincronizado». No se presenta una pantalla de error.

**6. Qué había en la base local:** 0 filas totales, 0 activas, 0 pendientes. Marca de sincronización: []. Consulta: SELECT id,nombre,precio,stock,pendiente,estado,categoriaId FROM Producto ORDER BY nombre. Archivo: consultas/esc4.json.

**7. Captura:** capturas/esc4_primera_apertura_sin_red.png

**8. Conclusión en una línea:** Una instalación sin caché informa que no tiene datos en vez de aparentar un fallo del inventario.

## E5. Sesión expirada sin red

**1. Identificador y nombre:** E5. Sesión expirada sin red

**2. Fecha y hora real:** 2026-10-23T14:22:58.982742-05:00

**3. Plataforma:** Android - Pixel 8 Pro, API 37.1

**4. Pasos exactos seguidos:** Arrancar temporalmente el backend con APP_JWT_EXPIRACION_MINUTOS=1. Iniciar sesión en Android, sincronizar y cerrar. Esperar el vencimiento emitido por el backend. Activar modo avión, forzar cierre y comprobar metadatos antes de reabrir. Abrir ya con jwt_exp=1792783267 y ahora=1792783361. Después reconectar y comprobar el login.

**5. Qué mostró la aplicación:** «Sesión vencida. Sin conexión: solo consulta local», seis productos activos, sin formulario ni botones de edición/eliminación y Sincronizar deshabilitado. El token se borra. Al reconectar se solicita iniciar sesión nuevamente.

**6. Qué había en la base local:** 10 filas totales, 6 activas, 0 pendientes. Producto observado: id=83, pendiente=0, precio=8.5, stock=17. Marca de sincronización: [{'id': 1, 'ultimaMillis': 1792783225926}]. Consulta: SELECT id,nombre,precio,stock,pendiente,estado,categoriaId FROM Producto ORDER BY nombre. Archivo: consultas/esc5_final.json.

**7. Captura:** capturas/esc5_consulta_local_verificada.png

**8. Conclusión en una línea:** Vencer la autorización bloquea los cambios, pero no impide consultar el inventario ya guardado en este dispositivo.

## Argumento del escenario 5

La versión anterior abría el login sin conexión aunque SQLite conservaba nueve filas. La captura anterior ilustra ese estado, pero no mide la expiración de su token. La prueba final sí usa un JWT genuino emitido con duración de un minuto, registra su exp y comprueba el vencimiento antes de reabrir. No se modificó su firma ni se falsificó un token. En el primer intento se abrió antes de vencer y se observó Inicio; se repitió después de verificar exp < ahora. Solo la repetición correcta se usa como evidencia final.

Mostrar datos locales en consulta es útil porque sin internet tampoco es posible autenticarse. No equivale a renovar la sesión: se borra el JWT vencido, no se conceden permisos, no se admiten mutaciones ni sincronización y al reconectar se exige login. Los datos quedan limitados al dispositivo de laboratorio; el usuario debe entender que son una copia con la antigüedad indicada. Cambios en de29d57; comprobaciones adicionales y revisión de expiración activa en c052b69.

La duración normal del backend se restauró a 3600 segundos (comprobacion-defensa.json). Se dejaron wifi y datos habilitados, modo avión desactivado, diez filas locales y cero pendientes. Las conclusiones quedan para revisión personal del estudiante.
