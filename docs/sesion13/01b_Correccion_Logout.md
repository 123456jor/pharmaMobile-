# Corrección adicional autorizada: cerrar sesión bloquea consulta local

El estudiante autorizó expresamente corregir el hueco 8 del recorrido. Se conserva la base, incluidos los pendientes; no se borra información para ocultar el problema.

`AlmacenSeguroAndroid` conserva en preferencias cifradas la bandera `consulta_bloqueada_logout`, escrita antes de completar el cierre. `AuthRepositorioRest.cerrarSesion` la activa y borra JWT/sesión/caché Bearer. `sesionGuardada` devuelve null inmediatamente cuando está bloqueada, incluso sin internet o al reiniciar el proceso. Un login correcto levanta el bloqueo. Credenciales incorrectas no lo levantan.

La expiración y los 401 utilizan `invalidarSesion`, que borra las credenciales pero no bloquea la consulta offline. `CerrarSesionUseCase` y `SesionViewModel` distinguen ambos motivos. No hay borrado ni modificación de productos en esta corrección. El comportamiento de iOS queda fuera de la verificación solicitada.

Se agregaron dos regresiones a `AuthRepositorioRestTest`: cierre voluntario seguido de una nueva instancia del repositorio sin red, y expiración frente a desbloqueo por login correcto. Se conservan las pruebas anteriores sin debilitarlas. Los resultados y la captura posterior se registran al ejecutar la verificación.

Resultado real: BUILD SUCCESSFUL; 96 pruebas, cero fallos/errores/omitidas. APK actualizado sin borrar datos. Se cerró sesión, se forzó el cierre del proceso y se reabrió en modo avión con Wi-Fi y datos apagados: aparece el login, no la consulta local. Captura 08c_logout_offline_bloqueado.png. La base conservó doce filas y un pendiente (08c_pendientes_conservados.json). Se restauró la conectividad. El primer ensayo automatizado falló por un DTO de prueba sin el campo obligatorio tipo; se completó el fixture y se repitió sin retirar comprobaciones.
