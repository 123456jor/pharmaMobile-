package pe.edu.upeu.pharmamobil.domain.permiso

enum class Permiso { CAMARA, UBICACION, NOTIFICACIONES }
sealed interface EstadoPermiso {
    data object Concedido : EstadoPermiso
    data object Denegado : EstadoPermiso
    data object DenegadoParaSiempre : EstadoPermiso
}
interface GestorPermisos {
    suspend fun estado(permiso: Permiso): EstadoPermiso
    suspend fun solicitar(permiso: Permiso): EstadoPermiso
    fun abrirAjustes()
}

/** Primera solicitud y denegación definitiva comparten rationale=false. */
fun resolverEstadoPermiso(concedido: Boolean, pedido: Boolean, explicar: Boolean): EstadoPermiso = when {
    concedido -> EstadoPermiso.Concedido
    pedido && !explicar -> EstadoPermiso.DenegadoParaSiempre
    else -> EstadoPermiso.Denegado
}

enum class AccionPermiso { Explicar, Solicitar, Continuar, AbrirAjustes }
fun accionPermiso(estado: EstadoPermiso, explicacionAceptada: Boolean): AccionPermiso = when (estado) {
    EstadoPermiso.Concedido -> AccionPermiso.Continuar
    EstadoPermiso.DenegadoParaSiempre -> AccionPermiso.AbrirAjustes
    EstadoPermiso.Denegado -> if (explicacionAceptada) AccionPermiso.Solicitar else AccionPermiso.Explicar
}
