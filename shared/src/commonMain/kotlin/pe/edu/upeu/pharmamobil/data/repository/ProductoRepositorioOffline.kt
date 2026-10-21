package pe.edu.upeu.pharmamobil.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import pe.edu.upeu.pharmamobil.data.local.ProductoLocal
import pe.edu.upeu.pharmamobil.data.remote.ProductoApi
import pe.edu.upeu.pharmamobil.data.remote.ejecutarLlamada
import pe.edu.upeu.pharmamobil.domain.auth.SesionManager
import pe.edu.upeu.pharmamobil.domain.error.ErrorApi
import pe.edu.upeu.pharmamobil.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository

class ProductoRepositorioOffline(
    private val local: ProductoLocal,
    private val remoto: ProductoApi,
    private val sesionManager: SesionManager? = null
) : ProductoRepository {
    private val sincronizacion = Mutex()

    override fun observar(): Flow<List<Producto>> = local.observar()
    override suspend fun listar(): List<Producto> = local.listar()

    override suspend fun registrar(producto: Producto): Producto {
        val pendiente = local.guardarPendiente(producto)
        try {
            sincronizar()
        } catch (fallo: ErrorApiException) {
            // Los rechazos del servidor y los errores de sesión deben ser visibles.
            if (fallo.error != ErrorApi.SinConexion && fallo.error != ErrorApi.TiempoAgotado) throw fallo
        }
        return pendiente
    }

    override suspend fun sincronizar() = sincronizacion.withLock {
        ejecutarLlamada(sesionManager) {
            subirPendientes()
            // Descargar todas las páginas evita perder productos fuera de la primera.
            val productos = mutableListOf<Producto>()
            var pagina = 0
            do {
                val respuesta = remoto.listar(pagina = pagina++)
                productos += respuesta.contenido.map { it.toDomain() }
            } while (!respuesta.ultima)
            local.reemplazarSincronizados(productos)
        }
    }

    private suspend fun subirPendientes() {
        local.pendientes().forEach { pendiente -> remoto.crear(pendiente.toRequest()) }
    }

    // Editar y eliminar requieren conexión, según la decisión del usuario.
    override suspend fun obtener(id: Long): Producto = ejecutarLlamada(sesionManager) {
        require(id > 0) { "Sincroniza el producto antes de editarlo" }
        remoto.obtener(id).toDomain()
    }

    override suspend fun actualizar(producto: Producto): Producto = sincronizacion.withLock {
        ejecutarLlamada(sesionManager) {
            require(producto.id > 0) { "Sincroniza el producto antes de editarlo" }
            remoto.actualizar(producto.id, producto.toRequest()).toDomain().also(local::guardarSincronizado)
        }
    }

    override suspend fun eliminar(id: Long) = sincronizacion.withLock {
        ejecutarLlamada(sesionManager) {
            require(id > 0) { "Sincroniza el producto antes de eliminarlo" }
            remoto.eliminar(id)
            local.listar().firstOrNull { it.id == id }?.let { local.guardarSincronizado(it.copy(estado = false)) }
            Unit
        }
    }
}
