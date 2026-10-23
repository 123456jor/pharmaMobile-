# Consulta local sin sesión vigente

El cambio autorizado permite consultar únicamente SQLite cuando auth/me no puede alcanzarse y no existe una sesión vigente. No se crea una sesión ni se conceden permisos. Si había token vencido se borra junto con la sesión cifrada. El ViewModel de consulta bloquea registrar, editar, eliminar y sincronizar, además de ocultar esos controles. Al recuperar conexión se comprueba auth/me cada cinco segundos y se vuelve al login; es necesario autenticarse para sincronizar.

Primera apertura sin red: se muestra el inventario vacío con explicación y «Aún no se ha sincronizado». La consulta local está limitada a los datos de este dispositivo, igual que en el escenario solicitado. No autoriza llamadas protegidas.

La prueba anterior de expiración se actualizó para exigir que el token siga borrándose y que la excepción solo habilite consulta local, sin sesión autenticada. Se añadieron pruebas de apertura sin token, reconexión sin cabecera Authorization y bloqueo de mutaciones en el ViewModel. Las pruebas de rechazo 401 y las seis offline permanecen activas.
