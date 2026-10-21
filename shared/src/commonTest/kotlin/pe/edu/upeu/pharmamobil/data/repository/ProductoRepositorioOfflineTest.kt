package pe.edu.upeu.pharmamobil.data.repository

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import pe.edu.upeu.pharmamobil.data.local.ProductoLocal
import pe.edu.upeu.pharmamobil.data.local.baseEnMemoria
import pe.edu.upeu.pharmamobil.data.local.crearDriverEnMemoria
import pe.edu.upeu.pharmamobil.data.remote.ProductoApi
import pe.edu.upeu.pharmamobil.data.remote.crearHttpClient
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.error.ErrorApi
import pe.edu.upeu.pharmamobil.domain.error.ErrorApiException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

class ProductoRepositorioOfflineTest {
    private val producto = Producto(0, "Ibuprofeno", 4.5, 20, categoriaId = 1)
    private val cabeceras = headersOf(HttpHeaders.ContentType, "application/json")
    private val remoto = """{"id":41,"nombre":"Ibuprofeno","precio":4.5,"stock":20,"categoriaId":1}"""
    private fun pagina(contenido: String = remoto, ultima: Boolean = true) =
        """{"contenido":[$contenido],"pagina":0,"tamanio":20,"totalElementos":1,"totalPaginas":1,"ultima":$ultima}"""

    private suspend fun escenario(engine: MockEngine, bloque: suspend (ProductoRepositorioOffline, ProductoLocal) -> Unit) {
        val driver = crearDriverEnMemoria()
        val local = ProductoLocal(baseEnMemoria(driver))
        val cliente = crearHttpClient(baseUrl = "http://test/api/v1/", engine = engine, esDepuracion = false)
        try { bloque(ProductoRepositorioOffline(local, ProductoApi(cliente)), local) }
        finally { cliente.close(); driver.close() }
    }

    @Test
    fun `sin red el producto queda pendiente y la pantalla lo ve`() = runTest {
        escenario(MockEngine { throw IOException("Sin red") }) { repo, _ ->
            repo.registrar(producto)
            assertEquals(1, repo.observar().first().size)
        }
    }

    @Test
    fun idsLocalesSonNegativosDistintosYConservanCategoria() = runTest {
        escenario(MockEngine { throw IOException("Sin red") }) { repo, local ->
            val uno = repo.registrar(producto)
            val dos = repo.registrar(producto.copy(nombre = "Otro"))
            assertTrue(uno.id < 0 && dos.id < uno.id)
            assertEquals(2, local.pendientes().size)
            assertTrue(local.pendientes().all { it.pendiente && it.categoriaId == 1L })
        }
    }

    @Test
    fun subirPendientesOcurreAntesDeBajar() = runTest {
        val peticiones = mutableListOf<HttpMethod>()
        escenario(MockEngine { solicitud ->
            peticiones += solicitud.method
            respond(if (solicitud.method == HttpMethod.Post) remoto else pagina(), headers = cabeceras)
        }) { repo, local ->
            local.guardarPendiente(producto)
            repo.sincronizar()
            assertEquals(listOf(HttpMethod.Post, HttpMethod.Get), peticiones)
        }
    }

    @Test
    fun sincronizarNoDejaDuplicadosNiPendientesHuerfanos() = runTest {
        var cantidadPost = 0
        escenario(MockEngine { solicitud ->
            if (solicitud.method == HttpMethod.Post) cantidadPost++
            respond(if (solicitud.method == HttpMethod.Post) remoto else pagina(), headers = cabeceras)
        }) { repo, local ->
            local.guardarPendiente(producto)
            repo.sincronizar()
            assertEquals(1, repo.observar().first().size, "No debe quedar la fila negativa junto a la del servidor")
            assertTrue(local.pendientes().isEmpty())
            assertEquals(41L, repo.observar().first().single().id)
            repo.sincronizar()
            assertEquals(1, cantidadPost, "Reintentar no debe volver a enviar lo ya confirmado")
        }
    }

    @Test
    fun reemplazarSincronizadosPreservaPendientes() = runTest {
        escenario(MockEngine { throw IOException("Sin red") }) { _, local ->
            val pendiente = local.guardarPendiente(producto)
            local.reemplazarSincronizados(listOf(producto.copy(id = 50)))
            assertEquals(setOf(pendiente.id, 50L), local.listar().map { it.id }.toSet())
            assertEquals(listOf(pendiente), local.pendientes())
        }
    }

    @Test
    fun falloPostConservaPendienteYNoEjecutaGet() = runTest {
        var cantidadGet = 0
        escenario(MockEngine { solicitud ->
            if (solicitud.method == HttpMethod.Get) cantidadGet++
            throw IOException("Conexión interrumpida")
        }) { repo, local ->
            repo.registrar(producto)
            assertEquals(1, local.pendientes().size)
            assertEquals(0, cantidadGet)
        }
    }

    @Test
    fun errorServidorNoSeSilenciaComoSinConexion() = runTest {
        escenario(MockEngine { respond("{}", HttpStatusCode.ServiceUnavailable, cabeceras) }) { repo, local ->
            val fallo = assertFailsWith<ErrorApiException> { repo.registrar(producto) }
            assertEquals(ErrorApi.Servidor, fallo.error)
            assertEquals(1, local.pendientes().size)
        }
    }

    @Test
    fun descargaTodasLasPaginas() = runTest {
        escenario(MockEngine { solicitud ->
            val primera = solicitud.url.parameters["pagina"] == "0"
            respond(pagina(if (primera) remoto else remoto.replace("41", "42"), ultima = !primera), headers = cabeceras)
        }) { repo, local ->
            repo.sincronizar()
            assertEquals(setOf(41L, 42L), local.listar().map { it.id }.toSet())
        }
    }
}
