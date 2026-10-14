package pe.edu.upeu.pharmamobil.data.repository

import pe.edu.upeu.pharmamobil.data.remote.ejecutarLlamada
import pe.edu.upeu.pharmamobil.data.remote.ProductoApi
import pe.edu.upeu.pharmamobil.data.remote.dto.ProductoRequestDto
import pe.edu.upeu.pharmamobil.data.remote.dto.ProductoResponseDto
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobil.domain.auth.SesionManager

class ProductoRepositorioRest(private val api: ProductoApi, private val sesionManager: SesionManager? = null) : ProductoRepository {
    override suspend fun listar(): List<Producto> = ejecutarLlamada(sesionManager) {
        api.listar().contenido.map(ProductoResponseDto::toDomain)
    }

    override suspend fun obtener(id: Long): Producto = ejecutarLlamada(sesionManager) { api.obtener(id).toDomain() }

    override suspend fun registrar(producto: Producto): Producto = ejecutarLlamada(sesionManager) {
        api.crear(producto.toRequest()).toDomain()
    }

    override suspend fun actualizar(producto: Producto): Producto = ejecutarLlamada(sesionManager) {
        api.actualizar(producto.id, producto.toRequest()).toDomain()
    }

    override suspend fun eliminar(id: Long) = ejecutarLlamada(sesionManager) { api.eliminar(id) }
}

fun ProductoResponseDto.toDomain() = Producto(
    id = id,
    nombre = nombre,
    precio = precio,
    stock = stock,
    estado = estado,
    categoriaId = categoriaId ?: 0L,
    categoriaNombre = categoriaNombre
)

fun Producto.toRequest() = ProductoRequestDto(
    nombre = nombre,
    precio = precio,
    stock = stock,
    estado = estado,
    categoriaId = categoriaId
)
