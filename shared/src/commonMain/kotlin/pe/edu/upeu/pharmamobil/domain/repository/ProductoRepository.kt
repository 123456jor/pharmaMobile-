package pe.edu.upeu.pharmamobil.domain.repository

import pe.edu.upeu.pharmamobil.domain.model.Producto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface ProductoRepository {
    fun observarUltimaSincronizacion(): Flow<Long?> = flow { emit(null) }
    /** La implementación offline observa exclusivamente SQLDelight. */
    fun observar(): Flow<List<Producto>> = flow { emit(listar()) }
    suspend fun sincronizar() { listar() }

    /** Incorpora el producto al inventario y devuelve el producto ya identificado. */
    suspend fun registrar(producto: Producto): Producto

    /** Entrega el inventario completo en el orden en que fue registrado. */
    suspend fun listar(): List<Producto>

    suspend fun obtener(id: Long): Producto
    suspend fun actualizar(producto: Producto): Producto
    suspend fun eliminar(id: Long)
}
