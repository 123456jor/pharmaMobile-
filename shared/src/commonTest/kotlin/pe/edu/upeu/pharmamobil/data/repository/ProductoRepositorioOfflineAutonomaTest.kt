package pe.edu.upeu.pharmamobil.data.repository

import io.ktor.client.engine.mock.MockEngine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import pe.edu.upeu.pharmamobil.data.local.ProductoLocal
import pe.edu.upeu.pharmamobil.data.local.baseEnMemoria
import pe.edu.upeu.pharmamobil.data.local.crearDriverEnMemoria
import pe.edu.upeu.pharmamobil.data.remote.ProductoApi
import pe.edu.upeu.pharmamobil.data.remote.crearHttpClient
import pe.edu.upeu.pharmamobil.domain.model.Producto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Cada caso abre y cierra su propia base; MockEngine evita red y emulador. */
class ProductoRepositorioOfflineAutonomaTest {
    private val nuevo = Producto(0, "Producto local autónoma", 5.5, 15, categoriaId = 1)

    private suspend fun conBaseNueva(
        motor: MockEngine,
        comprobar: suspend (ProductoRepositorioOffline, ProductoLocal) -> Unit
    ) {
        val driver = crearDriverEnMemoria()
        val local = ProductoLocal(baseEnMemoria(driver))
        val cliente = crearHttpClient(baseUrl = "http://prueba/api/v1/", engine = motor, esDepuracion = false)
        try { comprobar(ProductoRepositorioOffline(local, ProductoApi(cliente)), local) }
        finally { cliente.close(); driver.close() }
    }

    @Test
    fun prueba02RegistroSinRedConservaPendienteYLoEmiteElFlow() = runTest {
        conBaseNueva(MockEngine { throw IOException("Sin conexión") }) { repo, local ->
            val guardado = repo.registrar(nuevo)
            val emitido = repo.observar().first().single()
            assertTrue(guardado.id < 0)
            assertTrue(emitido.pendiente)
            assertEquals(guardado, emitido)
            assertEquals(listOf(guardado), local.pendientes())
        }
    }

    @Test
    fun prueba04LaBajadaNoBorraElProductoPendiente() = runTest {
        conBaseNueva(MockEngine { throw IOException("Sin conexión") }) { repo, local ->
            val pendiente = repo.registrar(nuevo)
            val remoto = nuevo.copy(id = 71, nombre = "Producto del servidor", pendiente = false)
            // Aislamos la bajada: un POST fallido detiene sincronizar antes del GET.
            // Probar el reemplazo local acredita que solo elimina pendiente=0.
            local.reemplazarSincronizados(listOf(remoto))
            val lista = repo.observar().first()
            assertEquals(2, lista.size)
            assertEquals(setOf(pendiente.id, remoto.id), lista.map { it.id }.toSet())
            assertEquals(pendiente, lista.single { it.id == pendiente.id })
            assertEquals(listOf(pendiente), local.pendientes())
        }
    }
}
