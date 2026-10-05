# Bitácora de errores controlados — Sesión 08

**Estado:** el contrato se contrastó con el código fuente de `H:\ProyectosUPEU\pharmaSoft`. Los resultados HTTP y de pantalla siguen pendientes: `localhost:8080/api/health` agotó el tiempo de espera y no se pudieron probar Oracle ni las operaciones CRUD reales.

En cada escenario registra la fecha/hora y plataforma, conserva el código HTTP recibido separado del `ErrorApi` producido, copia el texto real de pantalla y guarda la captura en la ruta prevista. Los cuerpos y códigos indicados abajo son esperados por la ficha, no resultados observados.

| ID / escenario | Pasos exactos y petición de apoyo | HTTP esperado según ficha | ErrorApi esperado según ficha | Resultado real |
| --- | --- | --- | --- | --- |
| 1. Validación del servidor | Enviar POST con nombre de 2 letras y categoría existente. Ejecutar `docs/scripts/01-validacion-nombre.http`. | 400 | `Validacion`; detalle debajo de Nombre | PENDIENTE DE EJECUCIÓN POR EL ESTUDIANTE |
| 2. Precio inválido | Ejecutar las dos peticiones del archivo `02-precio-invalido.http`, primero precio 0 y luego negativo. | 400 en ambas | `Validacion`; detalle debajo de Precio | PENDIENTE DE EJECUCIÓN POR EL ESTUDIANTE |
| 3. Recurso inexistente | Enviar PUT con ID que se haya verificado como inexistente. Ejecutar `03-recurso-inexistente.http`. | 404 | `NoEncontrado` | PENDIENTE DE EJECUCIÓN POR EL ESTUDIANTE |
| 4. Doble eliminación | Usar el mismo ID activo en dos DELETE consecutivos. `ProductoServiceImpl.eliminar` cambia `estado=false`; al repetirlo lanza `ReglaNegocioException`. Ejecutar `04-doble-eliminacion.http`. | 204 y luego 409 (inferido del código; respuesta real pendiente) | Segundo DELETE: `Conflicto`, mensaje esperado del backend: `El producto <nombre> ya se encuentra inactivo` | PENDIENTE DE EJECUCIÓN POR EL ESTUDIANTE |
| 5. Regla de negocio | La baja lógica no rechaza productos asociados a ventas. Usar un nombre de producto ya existente y ejecutar el POST duplicado de `05-regla-negocio.http`. | 409 (inferido del código; respuesta real pendiente) | `Conflicto`, mensaje esperado: `Ya existe un producto con el nombre <nombre>` | PENDIENTE DE EJECUCIÓN POR EL ESTUDIANTE |
| 6. Servidor caído | Detener PharmaSoft y ejecutar GET productos en la app. Ejecutar también `06-servidor-caido.http` como comparación. | Sin respuesta HTTP | `SinConexion` | PENDIENTE DE EJECUCIÓN POR EL ESTUDIANTE |
| 7. Tiempo agotado | Cambiar temporalmente `MODO_TIMEOUT_PARA_PRUEBAS` a `true` en `di/AppModule.kt`, ejecutar una operación y devolver el valor a `false`. `07-tiempo-agotado.ps1` muestra el comportamiento equivalente de curl. | Sin respuesta HTTP si vence antes de recibir respuesta | `TiempoAgotado` | PENDIENTE DE EJECUCIÓN POR EL ESTUDIANTE |
| 8. Cancelación | Iniciar una operación lenta y salir de la pantalla antes de la respuesta. `08-cancelacion.http` sirve solo para revisar el endpoint; la cancelación se provoca en la app. | Ninguno | Ninguno; se relanza `CancellationException` | PENDIENTE DE EJECUCIÓN POR EL ESTUDIANTE |

## Campos por completar tras ejecutar cada escenario

Para cada registro completa: **Fecha y hora**, **Plataforma (Android/iOS)**, **Código HTTP recibido** (o “Sin respuesta HTTP”), **Tipo de ErrorApi producido**, **Mensaje que vio el usuario**, **Captura**, y **Conclusión en una línea**. No copies aquí valores esperados como si fueran resultados.

Capturas esperadas: `docs/evidencias/errores/esc1-validacion-nombre-android.png`, `esc2-validacion-precio-android.png`, `esc3-no-encontrado-android.png`, `esc4-doble-eliminacion-android.png`, `esc5-conflicto-android.png`, `esc6-servidor-caido-android.png`, `esc7-tiempo-agotado-android.png` y `esc8-cancelacion-android.png`; repite con sufijo `-ios` al probar en iOS.

**Desajuste con la ficha:** la actividad propone que un producto asociado a ventas cause 409 al eliminar. El backend implementa baja lógica y conserva la fila para el historial, así que la asociación a ventas no bloquea el DELETE. Esta bitácora comprueba 409 con el escenario duplicado real del servidor y conserva el escenario de baja repetida como control. El resultado observado sigue pendiente.

**Cancelación en el código:** `EjecutarLlamada.kt` relanza `CancellationException` antes de capturar timeout, excepciones HTTP o IOException. `resultadoDe` también la relanza para que los casos de uso no la conviertan en `Result.failure`. La UI no tiene un catch genérico de cancelación. Si se atrapara como un fallo de red, la operación podría mostrar un error al abandonar la pantalla en vez de cancelarse. El comportamiento observado al navegar con una petición real sigue pendiente.
