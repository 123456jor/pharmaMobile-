package pe.edu.upeu.pharmamobil.presentation.producto

import androidx.lifecycle.viewModelScope
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.headersOf
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import pe.edu.upeu.pharmamobil.data.local.*
import pe.edu.upeu.pharmamobil.data.remote.*
import pe.edu.upeu.pharmamobil.data.repository.ProductoRepositorioOffline
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.port.Conectividad
import pe.edu.upeu.pharmamobil.domain.platform.FakeCompartidor
import pe.edu.upeu.pharmamobil.domain.usecase.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class EstadoSincronizacionTest {
    @AfterTest fun restaurarMain() = Dispatchers.resetMain()

    @Test fun estadoCombinaConsultasSqlFechaEnCursoYConectividadSinContarAMano() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        val driver = crearDriverEnMemoria()
        val base = baseEnMemoria(driver)
        val local = ProductoLocal(base)
        val puerta = CompletableDeferred<Unit>()
        val red = object : Conectividad { override val conectado = MutableStateFlow(true) }
        val cliente = crearHttpClient(baseUrl = "http://prueba/api/v1/", esDepuracion = false,
            engine = MockEngine {
                puerta.await()
                respond("""{"contenido":[],"pagina":0,"tamanio":20,"totalElementos":0,"totalPaginas":0,"ultima":true}""",
                    headers = headersOf("Content-Type", "application/json"))
            })
        val repo = ProductoRepositorioOffline(local, ProductoApi(cliente))
        val vm = ProductoViewModel(RegistrarProductoUseCase(repo), repo, ActualizarProductoUseCase(repo),
            EliminarProductoUseCase(repo), ObtenerProductoUseCase(repo), FakeCompartidor(), conectividad = red)
        try {
            assertTrue(vm.sincronizacion.first { it.enCurso }.enCurso)
            val uno = local.guardarPendiente(Producto(0, "Uno", 5.0, 2, categoriaId = 1))
            local.guardarPendiente(Producto(0, "Dos", 7.0, 3, categoriaId = 1))
            assertEquals(2, vm.sincronizacion.first { it.pendientes == 2 }.pendientes)
            local.marcarFallida(uno.id, "Validación")
            val fallida = vm.sincronizacion.first { it.pendientes == 1 && it.fallidas == 1 }
            assertEquals(1, fallida.fallidas)
            base.sincronizacionQueries.guardarUltima(123456)
            assertEquals(123456L, vm.sincronizacion.first { it.ultimaSync == 123456L }.ultimaSync)
            red.conectado.value = false
            assertFalse(vm.sincronizacion.first { !it.conectado }.conectado)
            puerta.complete(Unit)
            assertFalse(vm.sincronizacion.first { !it.enCurso }.enCurso)
            assertEquals(123456L, local.ultimaSincronizacion(), "No renueva fecha con filas sin enviar")
        } finally { vm.viewModelScope.cancel(); cliente.close(); driver.close() }
    }

    @Test fun lasTresMarcasYLaAccionDeConciliacionSeDerivanDeLaFila() {
        val producto = Producto(1, "Prueba", 5.0, 3, categoriaId = 1)
        assertEquals(EstadoEnvioUi.Sincronizado, producto.aUi().estadoEnvio)
        assertEquals(EstadoEnvioUi.Pendiente, producto.copy(id = -1, pendiente = true, intentos = 1).aUi().estadoEnvio)
        val fallida = producto.copy(id = -1, pendiente = true, intentos = 5, ultimoError =
            pe.edu.upeu.pharmamobil.domain.model.PoliticaEnvio.INCIERTO).aUi()
        assertEquals(EstadoEnvioUi.Fallido, fallida.estadoEnvio)
        assertTrue(fallida.requiereConciliacion)
        assertFalse(fallida.puedeReintentar)
    }
}
