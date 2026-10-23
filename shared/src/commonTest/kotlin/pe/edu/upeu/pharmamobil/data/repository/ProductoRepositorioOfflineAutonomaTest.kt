package pe.edu.upeu.pharmamobil.data.repository

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.headersOf
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import pe.edu.upeu.pharmamobil.data.local.ProductoLocal
import pe.edu.upeu.pharmamobil.data.local.baseEnMemoria
import pe.edu.upeu.pharmamobil.data.local.crearDriverEnMemoria
import pe.edu.upeu.pharmamobil.data.remote.ProductoApi
import pe.edu.upeu.pharmamobil.data.remote.crearHttpClient
import pe.edu.upeu.pharmamobil.data.remote.dto.PaginaResponseDto
import pe.edu.upeu.pharmamobil.data.remote.dto.ProductoResponseDto
import pe.edu.upeu.pharmamobil.domain.model.Producto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Cada caso abre y cierra su propia base; MockEngine evita red y emulador. */
class ProductoRepositorioOfflineAutonomaTest {
    private val nuevo = Producto(0, "Producto local autónoma", 5.5, 15, categoriaId = 1)
    private val cabeceras = headersOf(HttpHeaders.ContentType, "application/json")
    private fun Producto.dto() = ProductoResponseDto(id, nombre, precio, stock, estado, categoriaId, categoriaNombre)
    private fun pagina(productos: List<Producto>) = Json.encodeToString(
        PaginaResponseDto(productos.map { it.dto() }, 0, 100, productos.size.toLong(), 1, true)
    )

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

    @Test
    fun prueba01SincronizarConRedGuardaYEmiteLaListaCompleta() = runTest {
        val servidor = listOf(nuevo.copy(id = 41, nombre = "Servidor uno"), nuevo.copy(id = 42, nombre = "Servidor dos"))
        conBaseNueva(MockEngine { solicitud ->
            assertEquals(HttpMethod.Get, solicitud.method)
            respond(pagina(servidor), headers = cabeceras)
        }) { repo, local ->
            repo.sincronizar()
            val emitidos = repo.observar().first()
            assertEquals(servidor.toSet(), emitidos.toSet())
            assertEquals(2, emitidos.size)
            assertEquals(emitidos, local.listar())
            assertTrue(local.pendientes().isEmpty())
        }
    }

    @Test
    fun prueba03ReconectarEnviaElPendienteYLoConfirmaConIdReal() = runTest {
        var conRed = false
        var postAceptados = 0
        val confirmado = nuevo.copy(id = 72)
        conBaseNueva(MockEngine { solicitud ->
            if (!conRed) throw IOException("Sin conexión")
            if (solicitud.method == HttpMethod.Post) {
                postAceptados++
                respond(Json.encodeToString(confirmado.dto()), headers = cabeceras)
            } else respond(pagina(listOf(confirmado)), headers = cabeceras)
        }) { repo, local ->
            val pendiente = repo.registrar(nuevo)
            assertTrue(pendiente.id < 0 && local.pendientes().single().pendiente)
            // Se conserva la misma base y cambia solo la disponibilidad del servidor.
            conRed = true
            repo.sincronizar()
            assertEquals(1, postAceptados)
            assertTrue(local.pendientes().isEmpty())
            assertEquals(confirmado, repo.observar().first().single())
        }
    }

    @Test
    fun prueba05ProductoLocalYRemotoNoSeSobrescriben() = runTest {
        val existente = nuevo.copy(id = 41, nombre = "Producto del servidor", precio = 8.0)
        val confirmado = nuevo.copy(id = 72)
        conBaseNueva(MockEngine { solicitud ->
            respond(if (solicitud.method == HttpMethod.Post) Json.encodeToString(confirmado.dto())
                else pagina(listOf(existente, confirmado)), headers = cabeceras)
        }) { repo, local ->
            val pendiente = local.guardarPendiente(nuevo)
            local.guardarSincronizado(existente)
            assertEquals(setOf(pendiente.id, 41L), local.listar().map { it.id }.toSet())
            repo.sincronizar()
            val lista = repo.observar().first()
            assertEquals(2, lista.size)
            assertEquals(existente, lista.single { it.id == 41L })
            assertEquals(confirmado, lista.single { it.id == 72L })
        }
    }

    @Test
    fun prueba06SincronizarDosVecesConservaCantidadYNoRepitePost() = runTest {
        var postAceptados = 0
        val confirmado = nuevo.copy(id = 72)
        conBaseNueva(MockEngine { solicitud ->
            if (solicitud.method == HttpMethod.Post) {
                postAceptados++
                respond(Json.encodeToString(confirmado.dto()), headers = cabeceras)
            } else respond(pagina(listOf(confirmado)), headers = cabeceras)
        }) { repo, local ->
            local.guardarPendiente(nuevo)
            repo.sincronizar()
            val primera = repo.observar().first()
            repo.sincronizar()
            val segunda = repo.observar().first()
            assertEquals(1, primera.size)
            assertEquals(primera.size, segunda.size)
            assertEquals(primera, segunda)
            assertEquals(1, postAceptados)
            assertTrue(local.pendientes().isEmpty())
        }
    }
}
