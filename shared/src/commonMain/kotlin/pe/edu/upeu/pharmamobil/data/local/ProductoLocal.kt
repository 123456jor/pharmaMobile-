package pe.edu.upeu.pharmamobil.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pe.edu.upeu.pharmamobil.db.PharmaDb
import pe.edu.upeu.pharmamobil.db.Producto as ProductoFila
import pe.edu.upeu.pharmamobil.domain.model.Producto

/** Persistencia de dominio: no conoce HTTP ni DTO. */
class ProductoLocal(base: PharmaDb) {
    private val consultas = base.productoQueries

    fun observar(): Flow<List<Producto>> = consultas.listarTodos().asFlow()
        .mapToList(Dispatchers.Default).map { filas -> filas.map { it.aDominio() } }

    fun listar(): List<Producto> = consultas.listarTodos().executeAsList().map { it.aDominio() }
    fun pendientes(): List<Producto> = consultas.listarPendientes().executeAsList().map { it.aDominio() }

    fun guardarPendiente(producto: Producto): Producto = consultas.transactionWithResult {
        val menor = consultas.siguienteIdLocal().executeAsOne()
        val guardado = producto.copy(id = minOf(menor, 0L) - 1L, pendiente = true)
        insertar(guardado)
        guardado
    }

    fun reemplazarSincronizados(productos: List<Producto>) = consultas.transaction {
        consultas.borrarSincronizados()
        productos.forEach { insertar(it.copy(pendiente = false)) }
    }

    fun guardarSincronizado(producto: Producto) = insertar(producto.copy(pendiente = false))

    private fun insertar(producto: Producto) = with(producto) {
        consultas.insertar(id, nombre, precio, stock.toLong(), if (pendiente) 1L else 0L,
            if (estado) 1L else 0L, categoriaId, categoriaNombre)
    }
}

private fun ProductoFila.aDominio() = Producto(id, nombre, precio, stock.toInt(),
    estado != 0L, categoriaId, categoriaNombre, pendiente != 0L)
