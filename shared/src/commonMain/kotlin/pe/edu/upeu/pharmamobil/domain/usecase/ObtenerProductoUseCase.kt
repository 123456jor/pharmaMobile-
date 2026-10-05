package pe.edu.upeu.pharmamobil.domain.usecase

import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository

class ObtenerProductoUseCase(private val repository: ProductoRepository) {
    suspend operator fun invoke(id: Long): Result<Producto> = resultadoDe { repository.obtener(id) }
}
