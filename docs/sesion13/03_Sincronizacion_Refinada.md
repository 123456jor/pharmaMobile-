# Paso 3: sincronización refinada

## Ruta única y modelo

La aplicación ya sincronizaba en `ProductoRepositorioOffline.sincronizar`; no existía un SincronizarUseCase. Se refinó ese repositorio y se añadió una fachada de dominio que delega en él. El registro y el botón de sincronización usan el mismo Mutex y el mismo algoritmo. `ResultadoEnvio` y `ResumenSync` no importan HTTP. El resumen informa enviadas en este ciclo y los totales persistidos de fallidas/pendientes al terminar.

`resultadoDeError`, en datos, traduce ErrorApi.SinConexion/TiempoAgotado/Servidor a Transitorio y Validacion/NoEncontrado/Conflicto a Permanente. ErrorSesion.SinPermiso es permanente; SesionExpirada/CredencialesInvalidas se corresponden con «Sesión vencida». `ejecutarLlamada` sigue notificando el 401 al SesionManager; el ciclo detiene nuevos envíos con esa sesión.

## Intentos y orden

Cada ciclo procesa como máximo una petición por fila elegible, hasta cinco fallos persistidos. Un rechazo permanente fija el estado excluido y conserva el motivo. Un transitorio incrementa el contador anterior de forma transaccional y espera mediante `delay`, sin bloquear el hilo principal. La función exponencial recibe el número **anterior** de fallos: 0 da 1 s, luego 2, 4, 8 y 16. Después del quinto fallo no queda otro intento automático, por lo que no se duerme innecesariamente 16 s. El máximo de esperas efectivamente realizadas en cinco fallos es 1+2+4+8=15 s; no se afirma que se esperó 16 s tras agotar el máximo.

Se recorren todos los envíos antes de cualquier descarga; un rechazo de una fila no impide procesar las demás. Si se interrumpe el transporte no se inicia una descarga en esa conexión fallida. Con red se descargan todas las páginas. La conciliación local conserva pendientes/fallidas y evita sobrescribir sus IDs. La fecha solo avanza si no queda pendiente ni fallida y la descarga completa terminó bien. Los errores de la fila recién registrada llegan al formulario; los de otras filas no se atribuyen al campo actual.

## Creaciones no idempotentes y resultado incierto

Se inspeccionó el contrato de PharmaSoft (copia de referencia fff44f9): no hay soporte para Idempotency-Key. El nombre único no se presenta como protocolo idempotente.

Antes de llamar a POST se escribe durablemente en SQLDelight la marca excluida con el motivo de resultado incierto. Si se pierde la respuesta, se cancela la corrutina o muere el proceso, una apertura posterior no reproduce automáticamente esa creación. Si llega éxito, se confirma atómicamente el ID real. Si llega un rechazo HTTP conocido, se guarda su clasificación; un fallo transitorio recupera el contador anterior y suma uno en una sola transacción. Las pruebas comprueban que el bloqueo ya existe cuando se invoca el motor HTTP.

Sin red comprobada por `ConectividadAndroid`, se lanza EnvioNoIniciadoException **antes** de POST: puede quedar pendiente recuperable porque no se envió. Una IOException genérica o un timeout después de iniciar POST no acreditan eso; se bloquean para revisión aunque su clasificación de transporte sea transitoria. No hay un bucle que reproduzca POST automáticamente dentro de un ciclo.

La fila incierta ofrece dos salidas controladas por el usuario: consultar y conciliar un ID real (GET y comparación exacta de nombre, precio, stock y categoría), o confirmar que revisó el servidor y que no llegó antes de habilitar otro envío. Los rechazos de datos se corrigen sobre la misma fila; no se registran nuevamente como otro producto. La interfaz para estas acciones se incorpora en el paso 4. Nunca se borra silenciosamente la fila.

## Conectividad y regresión

El monitor Android usa ConnectivityManager con contexto de aplicación y ACCESS_NETWORK_STATE. Indica presencia de red, sin prometer disponibilidad del backend. Está detrás de una interfaz común. Su función aquí es distinguir un envío no iniciado de uno incierto; el paso 4 lo observará también en pantalla. No se simuló conectividad en la aplicación.

En la prueba anterior `prueba03ReconectarEnviaElPendienteYLoConfirmaConIdReal`, el fixture que representaba «sin conexión antes de enviar» ahora lanza EnvioNoIniciadoException en lugar de una IOException ambigua. Se conservan todas sus comprobaciones de reconexión, conservación, un solo POST aceptado e ID real. Es una precisión de la condición simulada: una respuesta perdida tiene su propia prueba y debe producir bloqueo. No se retira ni debilita la regresión para permitir reenvíos inseguros. El grafo Koin de prueba sustituye el monitor Android por un fake de prueba, como ya hacía con almacenamiento y compartidor.

## Pruebas

`SincronizacionRefinadaTest` cubre HTTP 503 con un incremento y una espera virtual de 1 s; HTTP 409 con motivo y sin segundo POST; máximo cinco y sin sexto; fórmula 1/2/4/8/16 y ausencia de espera final; bloqueo previo a transmisión; respuesta perdida y revisión explícita; recorrer envíos antes de GET; y traducción de errores tipados. Los resultados de la ejecución se agregan después de verificarlos.

Resultado real: BUILD SUCCESSFUL en 42 s; 107 pruebas, cero fallos, errores y omitidas; APK debug generado. Salida íntegra: evidencias/sesion13/pruebas/sync-pruebas.txt. La demostración visual de los nuevos estados y acciones corresponde al paso 4, todavía pendiente.
