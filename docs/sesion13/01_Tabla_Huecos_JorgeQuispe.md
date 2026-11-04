# Paso 1: recorrido real del flujo completo

Autor: Jorge Quispe. Fecha: 04/11/2026. Plataforma: emulador Pixel 8 Pro, Android. Se ejecutó sobre el APK de la línea base, sin cambios de producción. Antes de borrar datos se respaldó la base: tenía diez filas y ningún pendiente. Se conservó ese respaldo local fuera de Git.

Las capturas originales están en `../../evidencias/sesion13/capturas/`; cada PNG tiene XML de accesibilidad y fecha de obtención. Las consultas SQLite están en `../../evidencias/sesion13/pruebas/`. No son imágenes reconstruidas.

| N.º | Momento | Problema encontrado | Gravedad | Decisión | Solución | Captura |
|---|---|---|---|---|---|---|
| 1 | Primera apertura sin sesión | Sin hueco observado: aparece el login; no muestra datos protegidos con conexión | Sin incidencia | SE DESCARTA: comportamiento esperado | Mantener | 01_primera_apertura.png |
| 2 | Credenciales incorrectas | Sin hueco observado: informa «Usuario o contraseña incorrectos» | Sin incidencia | SE DESCARTA: mensaje específico observado | Conservar el mapeo de autenticación | 02_login_incorrecto.png; 02b_login_correcto.png |
| 3 | Base vacía y sin red | Sin hueco observado: estado vacío propio y «Aún no se ha sincronizado» | Sin incidencia | SE DESCARTA: no parece un fallo de carga | Conservar estado vacío | 03_inventario_vacio.png |
| 4 | Creación en modo avión | Guarda con ID -1 y marca pendiente; falta un contador global visible | Media | SE CIERRA HOY, pasos 2 y 4 | Consultas observables de pendientes y fallidas; indicadores en pantalla | 04_producto_pendiente.png; 04_registro_sin_red.json |
| 5 | Recuperación de señal | Sin pérdida ni duplicado observado: cambió a ID 103 y apareció automáticamente; faltan contadores globales | Media para presentación | SE CIERRA HOY, pasos 3 y 4 | Conservar orden de envío y descarga; agregar resumen, contadores y política segura de reintento | 05_producto_sincronizado.png; 05b_postman_producto_103.jpg |
| 6 | Validación del servidor | El precio cero recibe error bajo el campo, pero la fila rechazada permanece como pendiente sin explicar el motivo en la lista | Alta | SE CIERRA HOY, pasos 2–4 | Persistir último error, excluir fallos permanentes de reenvíos y permitir revisar/corregir esa misma fila | 06_precio_rechazado_servidor.png; 06_validacion_servidor.json |
| 7 | Vencimiento a mitad de formulario | No se perdió lo escrito en esta prueba: login con aviso y formulario conservado tras reautenticar dentro de la misma Activity | Sin incidencia en el caso probado | SE DESCARTA: no inventar pérdida de borrador; no se acredita supervivencia a muerte del proceso | Conservar y agregar regresión pertinente | 07a_borrador_antes_de_expirar.png; 07b_sesion_expirada.png; 07c_formulario_tras_reautenticar.png |
| 8 | Cierre de sesión | Borra el token y vuelve al login, pero al reiniciar sin red permite consultar el catálogo anterior como invitado | Alta | SE CIERRA HOY: el estudiante autorizó expresamente la corrección adicional | Diferenciar cierre voluntario de expiración; bloquear consulta tras logout sin borrar pendientes. Se implementará en commit propio | 08_cierre_sesion.png; 08b_cache_sin_sesion_tras_logout.png; 08_token_borrado.txt |

## Método y comprobaciones

Se desactivaron Wi-Fi y datos al activar modo avión; se consultó la base real con `run-as`, sin insertar filas desde scripts. El registro `A0_S13_Jorge_20261104_142729`, precio 5.50, stock 15 y categoría 1 se creó en la interfaz y se confirmó después mediante GET de Postman: HTTP 200, ID 103. La consulta del listado encontró una sola coincidencia; `05_confirmacion_backend.json` conserva la comprobación adicional sin credenciales.

Para la validación se registró `Validacion_S13_Jorge` con precio cero. La respuesta real del servidor se vio debajo de Precio. No se inventó un 400 en la app.

Para el vencimiento se reinició la copia local de PharmaSoft con la variable de entorno `APP_JWT_EXPIRACION_MINUTOS=1`, sin tocar el código del profesor ni alterar el JWT. Se inició sesión de nuevo y se escribió **Borrador_Jorge_S13**, precio 9.25, stock 8 y categoría 1, sin pulsar Registrar. El receptor debug existente guardó exclusivamente presencia del token, `exp` y hora, sin el token. En la captura previa quedaban aproximadamente ocho segundos hasta `exp`; después apareció el aviso real de expiración. Al reautenticar y abrir Productos se conservaron los cuatro campos. El borrador no se creó en el servidor. Esta prueba acredita el caso dentro del proceso, no recuperación tras cerrar la aplicación.

## Prioridades comprobadas

1. Rechazos permanentes visibles y persistidos: actualmente detienen nuevos envíos sin distinguirse de los pendientes recuperables.
2. Contadores observables y reintentos limitados con conciliación segura: falta esa presentación y política, aunque el envío básico funciona.
3. Consulta local después de logout explícito: observada y documentada; requiere la autorización adicional solicitada por el prompt. No se borra la base como atajo.

## Pruebas y límites

La línea base ejecutada hoy pasó 94 pruebas sin fallos, errores ni omitidas y generó el APK. En este paso no se modificó código de producción. No se ejecutó iOS por indicación del estudiante ni se dispone de evidencia en teléfono físico. Los restantes pasos aún no se declaran completados. El estudiante debe revisar y asumir esta tabla antes de su entrega.
