package pe.edu.upeu.pharmamobil.domain.repository

import pe.edu.upeu.pharmamobil.domain.model.Producto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import pe.edu.upeu.pharmamobil.domain.model.ResumenSync

interface ProductoRepository {
    fun observarUltimaSincronizacion(): Flow<Long?> = flow { emit(null) }
    /** La implementación offline observa exclusivamente SQLDelight. */
    fun observar(): Flow<List<Producto>> = flow { emit(listar()) }
    suspend fun sincronizar(): ResumenSync { listar(); return ResumenSync(0, 0, 0) }
    fun observarPendientes(): Flow<Int> = observar().map { filas -> filas.count { it.pendiente && it.intentos < Producto.MAX_INTENTOS } }
    fun observarFallidas(): Flow<Int> = observar().map { filas -> filas.count { it.pendiente && it.intentos >= Producto.MAX_INTENTOS } }
    suspend fun corregirPendiente(producto: Producto) { error("Este repositorio no admite corrección local") }
    suspend fun reintentarPendiente(id: Long, confirmaRevisionServidor: Boolean) { error("Este repositorio no admite reintentos locales") }
    suspend fun conciliarPendiente(idLocal: Long, idServidor: Long) { error("Este repositorio no admite conciliación local") }

    /** Incorpora el producto al inventario y devuelve el producto ya identificado. */
    suspend fun registrar(producto: Producto): Producto

    /** Entrega el inventario completo en el orden en que fue registrado. */
    suspend fun listar(): List<Producto>

    suspend fun obtener(id: Long): Producto
    suspend fun actualizar(producto: Producto): Producto
    suspend fun eliminar(id: Long)
}
