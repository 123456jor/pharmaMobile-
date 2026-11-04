# Paso 4: estado visible y acciones de recuperación

## Objetivo y clases

`SincronizacionUiState` reúne enCurso, pendientes, fallidas, ultimaSync y conectado. `ProductoViewModel.sincronizacion` combina los dos contadores observables SQLDelight, el trabajo en curso, la marca persistente y el monitor de red con `combine` y `stateIn(WhileSubscribed(5_000))`. La pantalla no cuenta ni modifica contadores manualmente y no lee un catálogo HTTP.

`ProductoScreen` muestra al entrar conexión, antigüedad, cantidades por enviar y que requieren atención. Mientras trabaja informa que está sincronizando. Cada fila tiene texto y símbolo diferentes: sincronizado, pendiente o requiere atención. La marca ausente usa el formateador existente «Aún no se ha sincronizado». Cambiar el texto de antigüedad no consulta la red.

Se conserva el formulario y CRUD anterior con un solo LazyColumn desplazable para que el inventario y sus acciones sean accesibles incluso con teclado o pantallas pequeñas. Editar y dar de baja productos confirmados siguen requiriendo conexión y permisos. Los productos locales se corrigen en su misma fila, sin hacer GET de un ID negativo ni crear un segundo producto.

`PharmaMobilTheme` usa el rojo #C8102E, oscuro #8B1023 y acento #E53935, fondo #F7F8FA, tarjetas blancas y texto #172033; conserva modo oscuro. Los ViewModels de productos autenticados se identifican por usuario, para que una cuenta distinta no herede el formulario de otra al cambiar sesión. El caso de reautenticar la misma cuenta conserva el comportamiento del borrador comprobado en el recorrido.

## Ver qué pasó, corregir y reintentar

`DetalleEnvioDialog` presenta el último motivo guardado. Un rechazo definitivo ofrece Revisar y corregir: carga los valores de esa fila, valida la corrección y actualiza el mismo ID local; no reintenta sin corregirla. Después se pulsa Sincronizar.

Una fila recuperable agotada o de resultado incierto ofrece Reintentar ahora solo con red y tras confirmar explícitamente que se revisó el servidor y no llegó. Reinicia el contador de esa fila y ejecuta la única sincronización. No restablece masivamente todos los fallos ni habilita automáticamente un rechazo de validación. Para un resultado incierto también permite conciliar un ID del servidor: GET y comprobación exacta antes de confirmar la fila, sin un nuevo POST. Estos controles están además validados en el repositorio; no dependen solo de deshabilitar un botón.

La consulta offline tras vencimiento sigue sin ofrecer escritura. Cerrar sesión voluntariamente mantiene el bloqueo persistente corregido en el commit 4f89b54.

## Verificación real

Se actualizó sin borrar la base. La validación de precio cero del recorrido inicial se volvió una fila fallida con motivo persistido; el encabezado indicó una fila que requiere atención. Se creó en modo avión real, con Wi-Fi y datos desactivados, `A00_S13_Offline_151900`, precio 6.75, stock 12 y categoría 1. La fila obtuvo ID -2 e intentos 1 por la comprobación sin red anterior a POST. El contador cambió automáticamente a un cambio por enviar, manteniendo una fallida.

Al recuperar señal y sincronizar, recibió ID real **123**, desapareció el provisional y el contador por enviar volvió a cero. La fallida anterior se conservó. La fecha no avanzó porque aún existe una fila sin resolver: no se presenta esa sincronización parcial como completamente actualizada.

Evidencias: `10_estado_sincronizacion_con_fallida`, `10b_contador_pendiente_sin_red`, `10c_contador_tras_sincronizar`; consultas `10_fallo_permanente_visible.json`, `10b_producto_ui_sin_red.json`, `10c_tras_recuperar_red.json`, y comprobación de radios apagados `10b_red_apagada.txt`. Las capturas se obtuvieron antes del ajuste gramatical menor de los contadores; son originales de la ejecución, no reconstrucciones.

## Pruebas

`EstadoSincronizacionTest` modifica la base en memoria y comprueba que el StateFlow refleja pendientes, fallidas, última fecha y trabajo en curso, además del cambio del monitor fake. También comprueba los tres estados visuales y la necesidad de conciliación a partir de la fila. Se conserva toda la regresión anterior. Los resultados finales y la revisión real del error se agregan tras verificarse.


## Resultado verificado del paso

Se abrió Ver qué pasó sobre Validacion_S13_Jorge: mostró «Validación: precio: El precio debe ser mayor que cero». Revisar y corregir cargó esa misma fila; se cambió el precio a 4.75. `10f_correccion_mismo_id.json` acredita ID -1, intentos 0 y trece filas: no se duplicó. Después de Sincronizar quedaron cero pendientes y cero fallidas; se renovó la fecha. Se forzó el cierre y se reabrió en modo avión sin desinstalar: se conservaron exactamente todas las filas y la marca de sincronización, comparadas entre `10g_todos_resueltos.json` y `10h_fecha_persistida.json`. Evidencias visuales 10d/10e/10f/10g/10h.

Pruebas reales finales: **109 aprobadas, cero fallos, errores y omitidas**; BUILD SUCCESSFUL. APK final compilado tras añadir desplazamiento automático al formulario al editar/corregir. Salidas íntegras `ui-pruebas-final.txt` y `ui-apk-final.txt`. Captura 10h tomada con ese APK, que también incluye el ajuste gramatical de contadores.
