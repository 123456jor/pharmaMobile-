package pe.edu.upeu.pharmamobil.domain.permiso

class FakeGestorPermisos : GestorPermisos {
    var actual: EstadoPermiso = EstadoPermiso.Denegado
    var respuesta: EstadoPermiso = EstadoPermiso.Concedido
    var solicitudes = 0
    var ajustes = 0
    override suspend fun estado(permiso: Permiso) = actual
    override suspend fun solicitar(permiso: Permiso): EstadoPermiso { solicitudes++; actual = respuesta; return actual }
    override fun abrirAjustes() { ajustes++ }
}
