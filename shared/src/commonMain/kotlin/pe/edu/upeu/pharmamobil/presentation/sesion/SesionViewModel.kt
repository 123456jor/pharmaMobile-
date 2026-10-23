package pe.edu.upeu.pharmamobil.presentation.sesion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobil.domain.auth.SesionManager
import pe.edu.upeu.pharmamobil.domain.model.Credenciales
import pe.edu.upeu.pharmamobil.domain.model.ConsultaLocalException
import pe.edu.upeu.pharmamobil.domain.model.ErrorSesion
import pe.edu.upeu.pharmamobil.domain.model.ErrorSesionException
import pe.edu.upeu.pharmamobil.domain.usecase.CerrarSesionUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.IniciarSesionUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ObtenerSesionGuardadaUseCase

class SesionViewModel(
    private val iniciar: IniciarSesionUseCase,
    private val obtenerGuardada: ObtenerSesionGuardadaUseCase,
    private val cerrar: CerrarSesionUseCase,
    manager: SesionManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(SesionUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            manager.eventos.collect { evento ->
                when (evento) {
                    ErrorSesion.SesionExpirada, ErrorSesion.CredencialesInvalidas -> cerrarSesion(sesionExpirada = true)
                    ErrorSesion.SinPermiso -> _uiState.update { it.copy(error = "No tienes permisos para realizar esta operación.") }
                    ErrorSesion.SinConexion -> _uiState.update { it.copy(error = "No hay conexión con PharmaSoft.") }
                }
            }
        }
    }

    fun onUsuarioChange(valor: String) { _uiState.update { it.copy(usuario = valor, error = null) } }
    fun onClaveChange(valor: String) { _uiState.update { it.copy(clave = valor, error = null) } }

    fun cargarSesionGuardada() {
        if (_uiState.value.cargando) return
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            obtenerGuardada().fold(
                onSuccess = { sesion -> _uiState.update { it.copy(cargando = false, sesion = sesion, consultaLocal = false,
                    aviso = if (it.sesionLocalExpirada && sesion == null) "Tu sesión expiró. Inicia sesión nuevamente." else it.aviso) } },
                onFailure = { fallo ->
                    if (fallo is ConsultaLocalException) {
                        _uiState.update { it.copy(cargando = false, sesion = null, consultaLocal = true,
                            sesionLocalExpirada = it.sesionLocalExpirada || fallo.sesionExpirada, error = null) }
                        return@fold
                    }
                    val expirada = (fallo as? ErrorSesionException)?.error == ErrorSesion.SesionExpirada
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            error = if (expirada) null else mensaje(fallo),
                            aviso = if (expirada) "Tu sesión expiró. Inicia sesión nuevamente." else it.aviso
                        )
                    }
                }
            )
        }
    }

    fun iniciarSesion() {
        val estado = _uiState.value
        if (estado.cargando) return
        if (estado.usuario.isBlank() || estado.clave.isBlank()) {
            _uiState.update { it.copy(error = "Ingresa tu usuario y contraseña.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null, aviso = null) }
            iniciar(Credenciales(estado.usuario.trim(), estado.clave)).fold(
                onSuccess = { sesion -> _uiState.update { it.copy(cargando = false, clave = "", sesion = sesion,
                    consultaLocal = false, sesionLocalExpirada = false) } },
                onFailure = { fallo -> _uiState.update { it.copy(cargando = false, error = mensaje(fallo)) } }
            )
        }
    }

    fun cerrarSesion(sesionExpirada: Boolean = false) {
        viewModelScope.launch {
            cerrar()
            _uiState.update {
                it.copy(
                    usuario = "", clave = "", cargando = false, error = null, sesion = null, consultaLocal = false,
                    aviso = if (sesionExpirada) "Tu sesión expiró. Inicia sesión nuevamente." else null
                )
            }
        }
    }

    fun limpiarAviso() { _uiState.update { it.copy(aviso = null) } }
    fun irAlLogin() { _uiState.update { it.copy(consultaLocal = false, cargando = false) } }

    private fun mensaje(fallo: Throwable): String = when ((fallo as? ErrorSesionException)?.error) {
        ErrorSesion.CredencialesInvalidas -> "Usuario o contraseña incorrectos."
        ErrorSesion.SesionExpirada -> "Tu sesión expiró. Inicia sesión nuevamente."
        ErrorSesion.SinPermiso -> "No tienes permisos para realizar esta operación."
        ErrorSesion.SinConexion -> "No hay conexión con PharmaSoft."
        null -> fallo.message ?: "No se pudo completar la operación."
    }
}
