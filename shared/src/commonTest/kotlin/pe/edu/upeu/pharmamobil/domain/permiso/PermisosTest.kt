package pe.edu.upeu.pharmamobil.domain.permiso

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.*
import pe.edu.upeu.pharmamobil.presentation.permiso.PermisosViewModel
import kotlin.test.*

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class PermisosTest {
    @AfterTest fun restaurar() = Dispatchers.resetMain()
    @Test fun primeraSolicitudNoSeConfundeConDenegacionDefinitiva() {
        assertEquals(EstadoPermiso.Denegado, resolverEstadoPermiso(false, false, false))
        assertEquals(EstadoPermiso.Denegado, resolverEstadoPermiso(false, true, true))
        assertEquals(EstadoPermiso.DenegadoParaSiempre, resolverEstadoPermiso(false, true, false))
        assertEquals(EstadoPermiso.Concedido, resolverEstadoPermiso(true, true, false))
    }
    @Test fun explicarSolicitarYOfrecerAjustesSonDecisionesComunes() {
        assertEquals(AccionPermiso.Explicar, accionPermiso(EstadoPermiso.Denegado, false))
        assertEquals(AccionPermiso.Solicitar, accionPermiso(EstadoPermiso.Denegado, true))
        assertEquals(AccionPermiso.Continuar, accionPermiso(EstadoPermiso.Concedido, false))
        assertEquals(AccionPermiso.AbrirAjustes, accionPermiso(EstadoPermiso.DenegadoParaSiempre, true))
    }
    @Test fun nuncaSolicitaAntesDeExplicarYTresResultadosNoSeFalsifican() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        for (respuesta in listOf(EstadoPermiso.Concedido, EstadoPermiso.Denegado, EstadoPermiso.DenegadoParaSiempre)) {
            val gestor = FakeGestorPermisos().apply { this.respuesta = respuesta }
            val vm = PermisosViewModel(gestor)
            try {
                vm.comprobar(); vm.solicitar()
                assertEquals(0, gestor.solicitudes)
                vm.preparar()
                assertTrue(vm.uiState.value.explicando)
                assertEquals(0, gestor.solicitudes)
                vm.solicitar(); advanceUntilIdle()
                assertEquals(1, gestor.solicitudes)
                assertEquals(respuesta, vm.uiState.value.estado)
                if (respuesta == EstadoPermiso.DenegadoParaSiempre) {
                    vm.preparar(); assertEquals(1, gestor.ajustes); assertEquals(1, gestor.solicitudes)
                }
            } finally { vm.viewModelScope.cancel() }
        }
    }
}
