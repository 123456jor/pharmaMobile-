# Paso 2: estado de envío en la base real

## Objetivo y archivos

Se amplía `producto`, sin inventar la tabla labor del ejemplo. `Producto.sq` describe el esquema actual con `intentos INTEGER NOT NULL DEFAULT 0` y `ultimoError TEXT`. Solo la nueva migración `2.sqm` contiene los dos ALTER TABLE. `1.sqm` se conserva intacta. La versión resultante es 3.

`Producto` conserva sus campos y agrega dos propiedades con valores por defecto. `ProductoLocal` adapta el mapeo y ofrece `porId`, `pendientesPorEnviar`, `sumarIntento`, `marcarFallida`, corrección de la misma fila y consultas observables de pendientes/fallidas. Las cuentas salen de SQL; la interfaz no las incrementará manualmente.

## Decisiones

- Pendiente recuperable: `pendiente=1 AND intentos<5`. Fallida: `pendiente=1 AND intentos>=5`. `marcarFallida` fija el límite y guarda el motivo, aunque el rechazo permanente ocurra en la primera petición. En ese caso 5 representa el estado agotado, **no cinco peticiones HTTP realizadas**.
- `sumarIntento` tiene condición SQL para no superar el máximo ni modificar una fila confirmada. Una fila fallida conserva datos y marca pendiente, queda visible y excluida de envío automático.
- La descarga solo borra filas confirmadas y no reemplaza una pendiente/fallida aunque el servidor enviara el mismo ID. La corrección local actualiza la fila existente, sin crear otro ID provisional.
- Se agregó un snapshot **vacío** del esquema instalado anterior (`sqldelight/databases/2.db`), sin usuarios ni datos de negocio, para la tarea `verifyCommonMainPharmaDbMigration`. La verificación compara el resultado migrado contra el esquema actual.

## Verificación

Antes de instalar el nuevo APK se cerró el proceso y se consultó la base real: versión 2, doce filas y un pendiente. Evidencia: `09_antes_migracion.json`. Se actualizará este apartado con la comparación posterior al instalar sin borrar datos.

Las pruebas nuevas cubren contadores observables, conservación del motivo, exclusión de fallidas, límite SQL, colisión de IDs al descargar, corrección sin duplicar y ejecución de `PharmaDb.Schema.migrate` sobre el esquema 2 con campos anteriores y marca de sincronización.

El primer intento de la tarea de verificación falló al cargar SQLite JDBC: el proceso trabajador eligió `C:\Windows` como carpeta temporal, provocando `AccessDeniedException` y después `UnsatisfiedLinkError`. No es una validación aprobada. Un segundo intento con `JAVA_TOOL_OPTIONS` no corrigió el trabajador; se está verificando con proceso nuevo, `_JAVA_OPTIONS` y TEMP/TMP locales. No se cambió ninguna dependencia para ocultar el error.


## Resultado real y pendiente explícito

Se instaló mediante `adb install -r`, sin pm clear ni desinstalar. AndroidSqliteDriver ejecutó la migración: versión 2 → 3. La comparación de todas las columnas anteriores fue idéntica en las doce filas; conserva un pendiente y la misma última sincronización. Las columnas nuevas quedaron en 0 y null. Evidencia completa: `09_antes_migracion.json`, `09_despues_migracion.json`, `09_comparacion_migracion.json` y captura `09_actualizacion_sin_borrar_datos.png`.

`testAndroidHostTest` y `assembleDebug`: BUILD SUCCESSFUL, **99 pruebas; cero fallos, errores y omitidas**. La tarea separada de verificación de SQLDelight falló tres veces por la carga nativa en C:\Windows. Se detuvieron los intentos y se pidió ayuda según el prompt. El estudiante encontró JAVA_HOME ausente en su terminal y recibió la ruta del JDK para repetir. **La verificación específica sigue pendiente; este paso no se presenta como completamente verificado**.

Activar además `verifyMigrations=true` en el generador reveló que el historial previo empieza en 1.sqm creando Sincronizacion, sin el CREATE histórico de producto. Se conservó el modo original del generador: no se inventó una migración 0 ni se alteró 1.sqm. La tarea explícita de verificación, según el código fuente de SQLDelight 2.1.0, compara los snapshots presentes con el esquema actual independientemente de esa opción. El snapshot vacío 2.db permite comprobar precisamente el salto 2 → 3. La prueba común ejecutó además la migración generada con datos y verificó su conservación.
