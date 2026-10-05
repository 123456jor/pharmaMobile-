package pe.edu.upeu.pharmamobil.domain.usecase

import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository

class ActualizarProductoUseCase(private val repository: ProductoRepository) {
    suspend operator fun invoke(
        id: Long,
        nombre: String,
        precio: String,
        stock: String,
        categoriaId: String
    ): Result<Producto> {
        val errores = validarEntradaProducto(nombre, precio, stock, categoriaId)
        if (errores.hayErrores) return Result.failure(ProductoInvalidoException(errores))
        return resultadoDe {
            repository.actualizar(
                Producto(id, nombre.trim(), precio.toDouble(), stock.toInt(), categoriaId = categoriaId.toLong())
            )
        }
    }
}

internal fun validarEntradaProducto(
    nombre: String,
    precio: String,
    stock: String,
    categoriaId: String
): ErroresDeProducto = ErroresDeProducto(
    nombre = if (nombre.isBlank()) "El nombre es obligatorio" else null,
    precio = when {
        precio.isBlank() -> "El precio es obligatorio"
        precio.toDoubleOrNull() == null || !precio.toDouble().isFinite() -> "El precio debe ser un número válido"
        else -> null // El precio mínimo lo valida PharmaSoft para poder mostrar su error real.
    },
    stock = when {
        stock.isBlank() -> "El stock es obligatorio"
        stock.toIntOrNull() == null -> "El stock debe ser un número entero"
        stock.toInt() < 0 -> "El stock no puede ser negativo"
        else -> null
    },
    categoriaId = when {
        categoriaId.isBlank() -> "Ingresa el ID de una categoría existente"
        categoriaId.toLongOrNull() == null || categoriaId.toLong() <= 0 -> "El ID de categoría debe ser positivo"
        else -> null
    }
)
