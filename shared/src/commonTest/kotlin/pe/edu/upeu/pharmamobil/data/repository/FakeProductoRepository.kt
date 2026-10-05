package pe.edu.upeu.pharmamobil.data.repository

import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository

/**
 * Doble del inventario para las pruebas: sin delay y capaz de fallar a
 * voluntad. Sin el, probar el camino de error del caso de uso o del ViewModel
 * era imposible, porque el repositorio en memoria nunca falla.
 */
class FakeProductoRepository(
    private val productos: MutableList<Producto> = mutableListOf()
) : ProductoRepository {

    var fallaAlRegistrar: Throwable? = null
    var fallaAlListar: Throwable? = null
    var fallaAlObtener: Throwable? = null
    var fallaAlActualizar: Throwable? = null
    var fallaAlEliminar: Throwable? = null
    var cantidadListados: Int = 0
    var antesDeRegistrar: (suspend () -> Unit)? = null
    var antesDeEliminar: (suspend () -> Unit)? = null

    private var siguienteId = 1L

    override suspend fun registrar(producto: Producto): Producto {

        antesDeRegistrar?.invoke()
        fallaAlRegistrar?.let { throw it }

        val guardado = producto.copy(id = siguienteId++)
        productos.add(guardado)
        return guardado
    }

    override suspend fun listar(): List<Producto> {

        fallaAlListar?.let { throw it }

        cantidadListados++

        return productos.toList()
    }

    override suspend fun obtener(id: Long): Producto {
        fallaAlObtener?.let { throw it }
        return productos.firstOrNull { it.id == id }
            ?: throw NoSuchElementException("Producto no encontrado")
    }

    override suspend fun actualizar(producto: Producto): Producto {
        fallaAlActualizar?.let { throw it }
        val indice = productos.indexOfFirst { it.id == producto.id }
        if (indice < 0) throw NoSuchElementException("Producto no encontrado")
        productos[indice] = producto
        return producto
    }

    override suspend fun eliminar(id: Long) {
        antesDeEliminar?.invoke()
        fallaAlEliminar?.let { throw it }
        val indice = productos.indexOfFirst { it.id == id }
        if (indice < 0) throw NoSuchElementException("Producto no encontrado")
        if (!productos[indice].estado) throw IllegalStateException("El producto ya se encuentra inactivo")
        productos[indice] = productos[indice].copy(estado = false)
    }
}
