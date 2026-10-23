package pe.edu.upeu.pharmamobil.presentation.sesion

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import pe.edu.upeu.pharmamobil.domain.auth.SesionManager
import pe.edu.upeu.pharmamobil.domain.model.*
import pe.edu.upeu.pharmamobil.domain.repository.AuthRepository
import pe.edu.upeu.pharmamobil.domain.usecase.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class SesionViewModelTest {
    @BeforeTest fun preparar() = Dispatchers.setMain(UnconfinedTestDispatcher())
    @AfterTest fun limpiar() = Dispatchers.resetMain()

    private class AuthControlado(val respuesta: CompletableDeferred<Sesion?>) : AuthRepository {
        // Fecha pasada deliberada: el test no espera al reloj real.
        override suspend fun iniciarSesion(credenciales: Credenciales) = Sesion("prueba", "admin", "Admin", emptySet(), 1)
        override suspend fun sesionGuardada(): Sesion? = respuesta.await()
        override suspend fun cerrarSesion() {}
    }
    private fun vm(respuesta: CompletableDeferred<Sesion?>): SesionViewModel {
        val repo = AuthControlado(respuesta)
        return SesionViewModel(IniciarSesionUseCase(repo), ObtenerSesionGuardadaUseCase(repo),
            CerrarSesionUseCase(repo), SesionManager()).also {
            it.onUsuarioChange("admin"); it.onClaveChange("clave"); it.iniciarSesion()
        }
    }

    @Test fun caducarBloqueaAntesDeEsperarHttpYEl401RetiraLaSesionAnterior() = runTest {
        val respuesta = CompletableDeferred<Sesion?>()
        val vm = vm(respuesta)
        try {
            assertNotNull(vm.uiState.value.sesion)
            vm.cargarSesionGuardada()
            runCurrent()
            assertNull(vm.uiState.value.sesion)
            assertTrue(vm.uiState.value.consultaLocal)
            assertTrue(vm.uiState.value.cargando)
            respuesta.completeExceptionally(ErrorSesionException(ErrorSesion.SesionExpirada))
            runCurrent()
            assertNull(vm.uiState.value.sesion)
            assertFalse(vm.uiState.value.consultaLocal)
            assertFalse(vm.uiState.value.cargando)
            assertTrue(vm.uiState.value.aviso!!.contains("expiró"))
        } finally { vm.viewModelScope.cancel() }
    }

    @Test fun caducarSinRedMantieneSoloConsultaSinRecuperarPermisos() = runTest {
        val respuesta = CompletableDeferred<Sesion?>()
        val vm = vm(respuesta)
        try {
            vm.cargarSesionGuardada()
            respuesta.completeExceptionally(ConsultaLocalException(true, Exception("Sin red")))
            runCurrent()
            assertNull(vm.uiState.value.sesion)
            assertTrue(vm.uiState.value.consultaLocal)
            assertTrue(vm.uiState.value.sesionLocalExpirada)
            assertFalse(vm.uiState.value.cargando)
        } finally { vm.viewModelScope.cancel() }
    }
}
