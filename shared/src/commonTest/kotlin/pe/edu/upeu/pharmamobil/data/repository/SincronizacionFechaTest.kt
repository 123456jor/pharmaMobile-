package pe.edu.upeu.pharmamobil.data.repository

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.headersOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import pe.edu.upeu.pharmamobil.data.local.ProductoLocal
import pe.edu.upeu.pharmamobil.data.local.baseEnMemoria
import pe.edu.upeu.pharmamobil.data.local.crearDriverEnMemoria
import pe.edu.upeu.pharmamobil.data.remote.ProductoApi
import pe.edu.upeu.pharmamobil.data.remote.crearHttpClient
import pe.edu.upeu.pharmamobil.domain.error.ErrorApiException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertFailsWith

class SincronizacionFechaTest {
    @Test fun fechaSeGuardaSoloTrasExitoYUnFalloNoLaRenueva() = runTest {
        val driver = crearDriverEnMemoria()
        val local = ProductoLocal(baseEnMemoria(driver))
        var hayRed = true
        var reloj = 123_456L
        val cliente = crearHttpClient(baseUrl = "http://prueba/api/v1/", esDepuracion = false,
            engine = MockEngine {
                if (!hayRed) throw IOException("Sin red")
                respond("""{"contenido":[],"pagina":0,"tamanio":20,"totalElementos":0,"totalPaginas":0,"ultima":true}""",
                    headers = headersOf("Content-Type", "application/json"))
            })
        try {
            val repo = ProductoRepositorioOffline(local, ProductoApi(cliente), ahora = { reloj })
            assertNull(local.ultimaSincronizacion())
            assertNull(repo.observarUltimaSincronizacion().first())
            repo.sincronizar()
            assertEquals(123_456L, local.ultimaSincronizacion())
            assertEquals(123_456L, repo.observarUltimaSincronizacion().first())
            hayRed = false
            reloj = 999_999
            assertFailsWith<ErrorApiException> { repo.sincronizar() }
            assertEquals(123_456L, local.ultimaSincronizacion())
        } finally { cliente.close(); driver.close() }
    }
}

