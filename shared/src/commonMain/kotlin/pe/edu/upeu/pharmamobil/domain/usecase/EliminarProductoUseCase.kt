package pe.edu.upeu.pharmamobil.domain.usecase

import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository

class EliminarProductoUseCase(private val repository: ProductoRepository) {
    suspend operator fun invoke(id: Long): Result<Unit> = resultadoDe { repository.eliminar(id) }
}
