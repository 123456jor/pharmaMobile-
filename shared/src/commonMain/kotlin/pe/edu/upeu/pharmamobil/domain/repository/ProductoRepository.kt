package pe.edu.upeu.pharmamobil.domain.repository

import pe.edu.upeu.pharmamobil.domain.model.Producto

interface ProductoRepository {

    /** Incorpora el producto al inventario y devuelve el producto ya identificado. */
    suspend fun registrar(producto: Producto): Producto

    /** Entrega el inventario completo en el orden en que fue registrado. */
    suspend fun listar(): List<Producto>

    suspend fun obtener(id: Long): Producto
    suspend fun actualizar(producto: Producto): Producto
    suspend fun eliminar(id: Long)
}
