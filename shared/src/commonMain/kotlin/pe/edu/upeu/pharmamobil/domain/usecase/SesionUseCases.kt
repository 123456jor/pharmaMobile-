package pe.edu.upeu.pharmamobil.domain.usecase

import pe.edu.upeu.pharmamobil.domain.model.Credenciales
import pe.edu.upeu.pharmamobil.domain.repository.AuthRepository

class IniciarSesionUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(credenciales: Credenciales) = resultadoDe { repository.iniciarSesion(credenciales) }
}

class ObtenerSesionGuardadaUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke() = resultadoDe { repository.sesionGuardada() }
}

class CerrarSesionUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke() = resultadoDe { repository.cerrarSesion() }
}
