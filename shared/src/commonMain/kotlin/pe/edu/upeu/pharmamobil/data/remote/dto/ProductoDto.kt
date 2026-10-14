package pe.edu.upeu.pharmamobil.data.remote.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.EncodeDefault

@Serializable
data class ProductoRequestDto(
    val nombre: String,
    val precio: Double,
    val stock: Int,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS) val estado: Boolean = true,
    val categoriaId: Long
)

@Serializable
data class ProductoResponseDto(
    val id: Long,
    val nombre: String,
    val precio: Double,
    val stock: Int,
    val estado: Boolean = true,
    val categoriaId: Long? = null,
    val categoriaNombre: String? = null
)

@Serializable
data class PaginaResponseDto<T>(
    val contenido: List<T>,
    val pagina: Int,
    val tamanio: Int,
    val totalElementos: Long,
    val totalPaginas: Int,
    val ultima: Boolean
)

@Serializable
data class ErrorResponseDto(
    val message: String? = null,
    val validationErrors: Map<String, String>? = null
)
