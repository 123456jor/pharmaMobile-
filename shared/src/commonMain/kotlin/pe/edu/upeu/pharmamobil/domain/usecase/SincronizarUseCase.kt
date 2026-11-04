package pe.edu.upeu.pharmamobil.domain.usecase

import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository

/** Fachada de la única sincronización existente; no contiene un segundo flujo HTTP. */
class SincronizarUseCase(private val repositorio: ProductoRepository) {
    suspend operator fun invoke() = repositorio.sincronizar()
}
