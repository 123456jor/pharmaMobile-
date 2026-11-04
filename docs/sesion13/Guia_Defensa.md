# Defensa de la práctica 13 - Jorge Quispe

## Cómo demostrarlo

1. Iniciar Oracle en Docker y PharmaSoft en IntelliJ (puerto 8080). Docker aloja el servidor; SQLDelight guarda el inventario dentro del teléfono.
2. Abrir Android, iniciar sesión y sincronizar. La app usa 10.0.2.2:8080, mientras Postman usa localhost:8080.
3. Desactivar Wi-Fi y datos del emulador. Crear un producto con nombre único. Debe aparecer pendiente con ID negativo y persistir al cerrar y reabrir.
4. Reconectar y pulsar Sincronizar. El ID cambia al del servidor y disminuye el contador. Comprobar ese ID en Postman con un login vigente. No repetir POST para comprobar existencia: usar GET.
5. Activar avisos: leer explicación, ver diálogo Android y comprobar los tres estados. Una denegación no bloquea el inventario. Si es definitiva se ofrece Ajustes.
6. Mostrar Firebase: el no fatal de prueba recibido, sus claves técnicas y registros; no mostrar tokens ni contraseñas.
7. Mostrar el reporte de 119 pruebas y las 25 comprobaciones de Postman; explicar los límites.

## Decisiones y respuestas

- **Fuente de verdad:** SQLDelight. La pantalla observa Flow; no necesita un GET para mostrar la caché.
- **Por qué Docker no reemplaza SQLDelight:** el servidor no puede consultarse al perder internet. La base local mantiene filas, intentos, error y fecha.
- **Orden:** enviar pendientes primero, descargar todas las páginas después. La descarga conserva filas sin resolver.
- **Reintentos:** errores transitorios usan espera 1, 2, 4 y 8 segundos antes de los siguientes intentos, hasta cinco fallos. No se espera tras el último. Un error permanente usa el valor límite como bloqueo; no significa que hizo cinco solicitudes.
- **POST incierto:** PharmaSoft no tiene clave de idempotencia. Se persiste el bloqueo antes de transmitir. Tras pérdida de respuesta o muerte del proceso no se reenvía automáticamente; se concilia un ID y sus campos o se revisa explícitamente el servidor.
- **Concurrencia:** Mutex impide dos sincronizaciones simultáneas. Prueba final comprueba una sola creación.
- **Sesión:** 401 detiene la subida y notifica la expiración. Logout voluntario bloquea la consulta local; el vencimiento conserva el modo de consulta permitido.
- **Permisos:** dominio contiene el contrato y tres estados; Android implementa Activity Result. La Activity se referencia débilmente y el puente se limpia al destruirla. El ViewModel no retiene Activity.
- **Firebase:** dominio conoce Telemetria; solo datos importa los SDK. Claves y valores se filtran y la excepción se sanea sin causa ni pila original.
- **Pruebas:** 119 ejecutadas, cero fallos, errores u omitidas. Migración SQLDelight aprobada y APK generado con 79 tareas realmente ejecutadas. Postman: 14 solicitudes y 25 comprobaciones aprobadas.
- **Límites:** Android emulado; iOS excluido. No se afirma prueba en teléfono físico ni envío push desde una campaña remota. La notificación demostrada es local y el token FCM se obtuvo sin publicarlo.

## Trazabilidad por paso

| Paso | Commit principal | Evidencia |
|---|---|---|
| 1 Auditoría y huecos | abae5a8 | 01_Tabla_Huecos_JorgeQuispe.md, capturas 01-08 |
| 2 Esquema | 30a6899 y afbd490 | 02_Esquema_Migracion.md, migración real 2 a 3 |
| 3 Sincronización | 7bdbe76 | 03_Sincronizacion_Refinada.md, pruebas virtuales |
| 4 Interfaz | d30930f | 04_Estados_Interfaz.md, capturas 10-10h |
| 5 Permisos | a8f45be | 05_Gestor_Permisos.md, capturas 11a-11i |
| 6 Firebase | 7467096 | 06_Integracion_Firebase.md, consola real |
| 7 Regresión | 15db0d7 | 07_Pruebas_Resultados.md, salida-pruebas.txt y HTML |

El PDF reúne las capturas originales con un índice SHA-256. El cotejo de los 14 criterios se encuentra en Matriz_Cumplimiento.md y al final del PDF. La rama se deja para revisión, sin fusionar automáticamente.


## Archivos para explicar los puntos delicados

- `data/repository/ProductoRepositorioOffline.kt`: el 409 se trata como permanente; la fila conserva el motivo y queda excluida del envío automático. Reintentar ahora solo rehabilita las filas recuperables según su estado; un resultado incierto exige conciliación o revisión explícita.
- `Producto.sq`, migración `2.sqm` y `data/local/ProductoLocal.kt`: guardan intentos y ultimoError por fila. Las consultas observables actualizan contadores sin sumarlos a mano.
- `domain/permiso/GestorPermisos.kt`: contrato sin Android. `GestorPermisosAndroid` conserva pedido_ para distinguir primera vez de rechazo definitivo. `PuentePermisos` conecta el resultado real del sistema y gestiona cancelación/ciclo de vida.
- `domain/port/Telemetria.kt`, `data/firebase/TelemetriaFirebase.kt` y `PrivacidadTelemetria.kt`: SDK en datos, listas de valores técnicos permitidos y excepción saneada.
- En commonTest se sustituyen el motor HTTP por MockEngine, permisos por FakeGestorPermisos y telemetría por FakeTelemetria. La base de cada prueba es independiente y el reloj de reintentos es virtual. Esto no sustituye la prueba real Android/Firebase/Postman, que tiene sus propias capturas.
- Los huecos iniciales y su resolución están en `01_Tabla_Huecos_JorgeQuispe.md`: error transitorio/permanente, intentos, contadores, permiso e integración. El acceso local posterior a logout se corrigió con autorización en `01b_Correccion_Logout.md`.

Revisar antes de exponer el [PR en borrador](https://github.com/123456jor/pharmaMobile-/pull/1). La entrega del aula y su aprobación corresponden al estudiante.
