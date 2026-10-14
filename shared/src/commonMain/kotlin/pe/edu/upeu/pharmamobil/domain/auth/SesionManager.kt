package pe.edu.upeu.pharmamobil.domain.auth

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import pe.edu.upeu.pharmamobil.domain.model.ErrorSesion

class SesionManager {
    private val _eventos = MutableSharedFlow<ErrorSesion>(extraBufferCapacity = 1)
    val eventos = _eventos.asSharedFlow()

    fun notificar(error: ErrorSesion) { _eventos.tryEmit(error) }
}
