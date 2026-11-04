package pe.edu.upeu.pharmamobil.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import app.cash.sqldelight.coroutines.mapToOne
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pe.edu.upeu.pharmamobil.db.PharmaDb
import pe.edu.upeu.pharmamobil.db.Producto as ProductoFila
import pe.edu.upeu.pharmamobil.domain.model.Producto

/** Persistencia de dominio: no conoce HTTP ni DTO. */
class ProductoLocal(base: PharmaDb) {
    private val consultas = base.productoQueries
    private val sincronizacion = base.sincronizacionQueries

    fun observarUltimaSincronizacion(): Flow<Long?> = sincronizacion.leerUltima().asFlow()
        .mapToOneOrNull(Dispatchers.Default)

    fun ultimaSincronizacion(): Long? = sincronizacion.leerUltima().executeAsOneOrNull()

    fun observar(): Flow<List<Producto>> = consultas.listarTodos().asFlow()
        .mapToList(Dispatchers.Default).map { filas -> filas.map { it.aDominio() } }

    fun listar(): List<Producto> = consultas.listarTodos().executeAsList().map { it.aDominio() }
    fun pendientes(): List<Producto> = consultas.listarPendientes().executeAsList().map { it.aDominio() }
    fun pendientesPorEnviar(maxIntentos: Int = Producto.MAX_INTENTOS): List<Producto> =
        consultas.pendientesPorEnviar(maxIntentos.toLong()).executeAsList().map { it.aDominio() }
    fun porId(id: Long): Producto? = consultas.porId(id).executeAsOneOrNull()?.aDominio()
    fun observarPendientes(maxIntentos: Int = Producto.MAX_INTENTOS): Flow<Int> =
        consultas.contarPendientes(maxIntentos.toLong()).asFlow().mapToOne(Dispatchers.Default).map(Long::toInt)
    fun observarFallidas(maxIntentos: Int = Producto.MAX_INTENTOS): Flow<Int> =
        consultas.contarFallidas(maxIntentos.toLong()).asFlow().mapToOne(Dispatchers.Default).map(Long::toInt)
    fun contarPendientes(): Int = consultas.contarPendientes(Producto.MAX_INTENTOS.toLong()).executeAsOne().toInt()
    fun contarFallidas(): Int = consultas.contarFallidas(Producto.MAX_INTENTOS.toLong()).executeAsOne().toInt()
    fun sumarIntento(id: Long, motivo: String) = consultas.sumarIntento(motivo, id, Producto.MAX_INTENTOS.toLong())
    /** Recupera el contador anterior solo tras un rechazo conocido; todo sigue siendo atómico. */
    fun registrarFalloTransitorio(id: Long, anteriores: Int, motivo: String) = consultas.transaction {
        require(anteriores in 0 until Producto.MAX_INTENTOS)
        consultas.restaurarIntentosTrasRechazo(anteriores.toLong(), id)
        sumarIntento(id, motivo)
    }
    /** El límite es también el estado excluido de envío. No significa cinco peticiones reales. */
    fun marcarFallida(id: Long, motivo: String) = consultas.marcarFallida(Producto.MAX_INTENTOS.toLong(), motivo, id)
    fun reiniciarIntentos(id: Long) = consultas.reiniciarIntentos(id)
    fun corregirPendiente(producto: Producto) = with(producto) {
        consultas.corregirPendiente(nombre, precio, stock.toLong(), categoriaId, id)
    }

    fun guardarPendiente(producto: Producto): Producto = consultas.transactionWithResult {
        val menor = consultas.siguienteIdLocal().executeAsOne()
        val guardado = producto.copy(id = minOf(menor, 0L) - 1L, pendiente = true)
        insertar(guardado)
        guardado
    }

    fun reemplazarSincronizados(productos: List<Producto>) = consultas.transaction {
        consultas.borrarSincronizados()
        // Una respuesta remota nunca puede reemplazar una fila local sin confirmar.
        productos.filter { porId(it.id)?.pendiente != true }.forEach {
            insertar(it.copy(pendiente = false, intentos = 0, ultimoError = null))
        }
    }

    /** Lista y marca se confirman juntas; una descarga incompleta no renueva la fecha. */
    fun confirmarSincronizacion(productos: List<Producto>, ahora: Long) = consultas.transaction {
        reemplazarSincronizados(productos)
        sincronizacion.guardarUltima(ahora)
    }

    fun guardarSincronizado(producto: Producto) {
        check(porId(producto.id)?.pendiente != true) { "No se puede sobrescribir un envío local sin confirmar" }
        insertar(producto.copy(pendiente = false, intentos = 0, ultimoError = null))
    }

    /** Confirmar solo después del POST: sustituye temporal por id real atómicamente. */
    fun confirmarSubida(idLocal: Long, confirmado: Producto) = consultas.transaction {
        consultas.borrarPorId(idLocal)
        guardarSincronizado(confirmado)
    }

    private fun insertar(producto: Producto) = with(producto) {
        consultas.insertar(id, nombre, precio, stock.toLong(), if (pendiente) 1L else 0L,
            if (estado) 1L else 0L, categoriaId, categoriaNombre, intentos.toLong(), ultimoError)
    }
}

private fun ProductoFila.aDominio() = Producto(id, nombre, precio, stock.toInt(),
    estado != 0L, categoriaId, categoriaNombre, pendiente != 0L, intentos.toInt(), ultimoError)
