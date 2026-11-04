package pe.edu.upeu.pharmamobil.data.repository

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.io.IOException
import pe.edu.upeu.pharmamobil.data.local.*
import pe.edu.upeu.pharmamobil.data.remote.*
import pe.edu.upeu.pharmamobil.domain.error.*
import pe.edu.upeu.pharmamobil.domain.model.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class SincronizacionRefinadaTest {
    private val producto = Producto(0, "Registro pendiente", 5.5, 15, categoriaId = 1)
    private val cabeceras = headersOf("Content-Type", "application/json")
    private val vacia = """{"contenido":[],"pagina":0,"tamanio":20,"totalElementos":0,"totalPaginas":0,"ultima":true}"""
    private suspend fun escenario(engine: MockEngine, bloque: suspend (ProductoRepositorioOffline, ProductoLocal) -> Unit) {
        val driver = crearDriverEnMemoria()
        val local = ProductoLocal(baseEnMemoria(driver))
        val cliente = crearHttpClient(baseUrl = "http://prueba/api/v1/", engine = engine, esDepuracion = false)
        try { bloque(ProductoRepositorioOffline(local, ProductoApi(cliente)), local) }
        finally { cliente.close(); driver.close() }
    }

    @Test fun unFalloTransitorioSumaIntentoYNoMarcaFallida() = runTest {
        escenario(MockEngine { r ->
            respond(if (r.method == HttpMethod.Post) "{}" else vacia,
                if (r.method == HttpMethod.Post) HttpStatusCode.ServiceUnavailable else HttpStatusCode.OK, cabeceras)
        }) { repo, local ->
            val fila = local.guardarPendiente(producto)
            val inicio = currentTime
            assertEquals(ResumenSync(0, 0, 1), repo.sincronizar())
            assertEquals(1, local.porId(fila.id)?.intentos)
            assertEquals(1000L, currentTime - inicio)
            assertTrue(local.porId(fila.id)!!.pendiente)
            assertNotNull(local.porId(fila.id)?.ultimoError)
            assertNull(local.ultimaSincronizacion())
        }
    }

    @Test fun unFalloPermanenteMarcaLaFilaYNoLaReintenta() = runTest {
        var posts = 0
        escenario(MockEngine { r ->
            if (r.method == HttpMethod.Post) { posts++; respond("""{"message":"Duplicado"}""", HttpStatusCode.Conflict, cabeceras) }
            else respond(vacia, headers = cabeceras)
        }) { repo, local ->
            val fila = local.guardarPendiente(producto)
            assertEquals(ResumenSync(0, 1, 0), repo.sincronizar())
            repo.sincronizar()
            assertEquals(1, posts)
            assertTrue(local.porId(fila.id)!!.pendiente)
            assertContains(local.porId(fila.id)!!.ultimoError!!, "Duplicado")
        }
    }

    @Test fun cincoIntentosPersistidosSinSextoNiEsperaDespuesDelUltimo() = runTest {
        var posts = 0
        escenario(MockEngine { r ->
            if (r.method == HttpMethod.Post) { posts++; respond("{}", HttpStatusCode.ServiceUnavailable, cabeceras) }
            else respond(vacia, headers = cabeceras)
        }) { repo, local ->
            val fila = local.guardarPendiente(producto)
            val esperas = listOf(1000L, 2000L, 4000L, 8000L, 0L)
            repeat(5) { indice ->
                val inicio = currentTime
                repo.sincronizar()
                assertEquals(esperas[indice], currentTime - inicio)
                assertEquals(indice + 1, local.porId(fila.id)?.intentos)
            }
            repo.sincronizar()
            assertEquals(5, posts)
            assertEquals(1, local.contarFallidas())
            assertEquals(0, local.contarPendientes())
            assertEquals(15000L, currentTime)
        }
    }

    @Test fun formulaDeEsperaIncluyeDieciseisYAcotaElDesplazamiento() {
        assertEquals(listOf(1000L, 2000L, 4000L, 8000L, 16000L), (0..4).map(::esperaEntreIntentos))
        assertEquals(16000L, esperaEntreIntentos(100))
        assertEquals(1000L, esperaEntreIntentos(-1))
    }

    @Test fun respuestaPerdidaSeBloqueaAntesDePostYOtroCicloNoReenvia() = runTest {
        var posts = 0
        escenario(MockEngine { r ->
            if (r.method == HttpMethod.Post) { posts++; throw IOException("Respuesta perdida después de aceptar") }
            else respond(vacia, headers = cabeceras)
        }) { repo, local ->
            val fila = local.guardarPendiente(producto)
            repo.sincronizar()
            assertEquals(1, local.contarFallidas())
            assertEquals(ProductoRepositorioOffline.MOTIVO_INCIERTO, local.porId(fila.id)?.ultimoError)
            repo.sincronizar()
            assertEquals(1, posts)
            assertFailsWith<IllegalArgumentException> { repo.reintentarPendiente(fila.id, false) }
            assertFailsWith<IllegalArgumentException> { repo.corregirPendiente(fila.copy(precio = 6.0)) }
            repo.reintentarPendiente(fila.id, true)
            assertEquals(0, local.porId(fila.id)?.intentos)
        }
    }

    @Test fun bloqueoDuraderoExisteAntesDeInvocarElMotorHttp() = runTest {
        val driver = crearDriverEnMemoria()
        val local = ProductoLocal(baseEnMemoria(driver))
        val fila = local.guardarPendiente(producto)
        val cliente = crearHttpClient(baseUrl = "http://prueba/api/v1/", esDepuracion = false, engine = MockEngine {
            assertEquals(Producto.MAX_INTENTOS, local.porId(fila.id)?.intentos)
            assertEquals(ProductoRepositorioOffline.MOTIVO_INCIERTO, local.porId(fila.id)?.ultimoError)
            throw IOException("Interrupción")
        })
        try {
            ProductoRepositorioOffline(local, ProductoApi(cliente)).sincronizar()
            assertTrue(local.pendientesPorEnviar().isEmpty())
        } finally { cliente.close(); driver.close() }
    }

    @Test fun rechazoDeUnaFilaNoImpideSubirLasDemasYLaBajadaEsLaUltima() = runTest {
        val orden = mutableListOf<HttpMethod>()
        escenario(MockEngine { r ->
            orden += r.method
            if (r.method == HttpMethod.Post) respond("{}", HttpStatusCode.BadRequest, cabeceras)
            else respond(vacia, headers = cabeceras)
        }) { repo, local ->
            local.guardarPendiente(producto)
            local.guardarPendiente(producto.copy(nombre = "Otra fila"))
            repo.sincronizar()
            assertEquals(listOf(HttpMethod.Post, HttpMethod.Post, HttpMethod.Get), orden)
            assertEquals(2, local.listar().size)
            assertEquals(2, local.contarFallidas())
        }
    }

    @Test fun mapeoTipadoIncluyeTransporteValidacionYAutorizacion() {
        listOf(ErrorApi.SinConexion, ErrorApi.TiempoAgotado, ErrorApi.Servidor).forEach {
            assertIs<ResultadoEnvio.Transitorio>(resultadoDeError(ErrorApiException(it)))
        }
        listOf(ErrorApi.Validacion(mapOf("precio" to "Debe ser positivo")), ErrorApi.NoEncontrado,
            ErrorApi.Conflicto("Duplicado")).forEach {
            assertIs<ResultadoEnvio.Permanente>(resultadoDeError(ErrorApiException(it)))
        }
        assertEquals(ResultadoEnvio.Permanente("Sesión vencida"), resultadoDeError(ErrorSesionException(ErrorSesion.SesionExpirada)))
        assertIs<ResultadoEnvio.Permanente>(resultadoDeError(ErrorSesionException(ErrorSesion.SinPermiso)))
        assertFalse(resultadoPostIncierto(ErrorApiException(ErrorApi.SinConexion, EnvioNoIniciadoException())))
        assertTrue(resultadoPostIncierto(ErrorApiException(ErrorApi.TiempoAgotado)))
    }
}
