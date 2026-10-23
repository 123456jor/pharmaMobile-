# Consulta local sin sesión vigente

El cambio autorizado permite consultar únicamente SQLite cuando auth/me no puede alcanzarse y no existe una sesión vigente. No se crea una sesión ni se conceden permisos. Si había token vencido se borra junto con la sesión cifrada. El ViewModel de consulta bloquea registrar, editar, eliminar y sincronizar, además de ocultar esos controles. Al recuperar conexión se comprueba auth/me cada cinco segundos y se vuelve al login; es necesario autenticarse para sincronizar.

Primera apertura sin red: se muestra el inventario vacío con explicación y «Aún no se ha sincronizado». La consulta local está limitada a los datos de este dispositivo, igual que en el escenario solicitado. No autoriza llamadas protegidas.

La prueba anterior de expiración se actualizó para exigir que el token siga borrándose y que la excepción solo habilite consulta local, sin sesión autenticada. Se añadieron pruebas de apertura sin token, reconexión sin cabecera Authorization y bloqueo de mutaciones en el ViewModel. Las pruebas de rechazo 401 y las seis offline permanecen activas.
## Verificación adicional

Las pruebas 2 y 4 se ejecutaron alterando temporalmente guardarPendiente para guardar pendiente=false. Ambas FALLARON, tal como debía ocurrir. El archivo original se restauró automáticamente en finally y git diff confirmó que no quedó el defecto. La salida sensibilidad-fallo-esperado.txt conserva las dos aserciones fallidas. Ese cambio nunca se confirmó.

La suite completa se repitió después de restaurar: 91 pruebas, 0 fallos, 0 errores y 0 omitidas. Incluye las seis obligatorias y pruebas adicionales de antigüedad y consulta local. El APK debug también compiló. La salida íntegra está en salida-pruebas.txt y el informe HTML en reporte-pruebas/index.html.

Se añadió la revisión de expiración mientras la app permanece abierta para solicitar la reevaluación de sesión al alcanzar la fecha guardada. Se compila junto con la corrección; la evidencia manual E5 documenta específicamente la reapertura posterior al vencimiento.

## Cierre de sesión durante la reevaluación

La revisión final añadió bloqueo inmediato si la fecha vence mientras la app sigue abierta: no se mantiene la sesión anterior durante HTTP. Un 401 retira explícitamente la sesión previa. El repositorio borra el JWT vencido antes de comprobar auth/me, por lo que no adjunta esa credencial. Se agregaron dos pruebas de SesionViewModel y una de ausencia de Authorization con JWT vencido. Suite final: 94/94, cero fallos, errores u omitidas. La evidencia E5 comprueba la reapertura; estas pruebas adicionales comprueban la transición mientras la app permanece abierta sin esperas reales.
