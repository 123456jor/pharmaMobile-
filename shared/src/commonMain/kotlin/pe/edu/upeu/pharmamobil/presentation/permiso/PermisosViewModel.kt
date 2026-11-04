package pe.edu.upeu.pharmamobil.presentation.permiso

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobil.domain.permiso.*

data class PermisoUiState(val estado: EstadoPermiso = EstadoPermiso.Denegado,
    val explicando: Boolean = false, val ocupado: Boolean = false, val error: String? = null)

class PermisosViewModel(private val gestor: GestorPermisos) : ViewModel() {
    private val mutable = MutableStateFlow(PermisoUiState())
    val uiState = mutable.asStateFlow()
    fun comprobar() {
        if (mutable.value.ocupado) return
        viewModelScope.launch { mutable.update { it.copy(estado = gestor.estado(Permiso.NOTIFICACIONES)) } }
    }
    fun preparar() {
        when (accionPermiso(mutable.value.estado, false)) {
            AccionPermiso.Explicar -> mutable.update { it.copy(explicando = true, error = null) }
            AccionPermiso.AbrirAjustes -> gestor.abrirAjustes()
            else -> Unit
        }
    }
    fun cancelarExplicacion() { mutable.update { it.copy(explicando = false) } }
    fun solicitar() {
        if (mutable.value.ocupado || !mutable.value.explicando) return
        viewModelScope.launch {
            mutable.update { it.copy(ocupado = true, explicando = false, error = null) }
            try { val estado = gestor.solicitar(Permiso.NOTIFICACIONES); mutable.update { it.copy(estado = estado) } }
            catch (cancelacion: CancellationException) { throw cancelacion }
            catch (_: Exception) { mutable.update { it.copy(error = "No se pudo abrir la solicitud. Vuelve a intentarlo.") } }
            finally { mutable.update { it.copy(ocupado = false) } }
        }
    }
}
