package pe.edu.upeu.pharmamobil.data.repository

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobil.data.local.*
import pe.edu.upeu.pharmamobil.data.remote.*
import pe.edu.upeu.pharmamobil.domain.auth.SesionManager
import pe.edu.upeu.pharmamobil.domain.model.*
import kotlin.test.*

class IntegradorRegresionFinalTest {
    private val nuevo = Producto(0, "Producto de regresión", 5.5, 15, categoriaId = 1)
    private val headers = headersOf("Content-Type", "application/json")
    private val confirmado = """{"id":99,"nombre":"Producto de regresión","precio":5.5,"stock":15,"estado":true,"categoriaId":1}"""
    private fun pagina(contenido: String = "") = """{"contenido":[$contenido],"pagina":0,"tamanio":20,"totalElementos":0,"totalPaginas":1,"ultima":true}"""

    @Test fun nuevasInstanciasLeenIntentosPersistidosYNoHacenSextoPost() = runTest {
        val driver = crearDriverEnMemoria()
        val base = baseEnMemoria(driver)
        var posts = 0
        val cliente = crearHttpClient(baseUrl = "http://prueba/api/v1/", esDepuracion = false, engine = MockEngine {
            if (it.method == HttpMethod.Post) { posts++; respond("{}", HttpStatusCode.ServiceUnavailable, headers) }
            else respond(pagina(), headers = headers)
        })
        try {
            val fila = ProductoLocal(base).guardarPendiente(nuevo)
            repeat(6) { indice ->
                val localNuevo = ProductoLocal(base)
                ProductoRepositorioOffline(localNuevo, ProductoApi(cliente)).sincronizar()
                assertEquals(minOf(indice + 1, 5), localNuevo.porId(fila.id)?.intentos)
            }
            assertEquals(5, posts)
            assertEquals(1, ProductoLocal(base).contarFallidas())
        } finally { cliente.close(); driver.close() }
    }

    @Test fun dosSincronizacionesConcurrentesNoDuplicanLaCreacion() = runTest {
        val driver = crearDriverEnMemoria()
        val local = ProductoLocal(baseEnMemoria(driver))
        val orden = mutableListOf<HttpMethod>()
        var posts = 0
        val cliente = crearHttpClient(baseUrl = "http://prueba/api/v1/", esDepuracion = false, engine = MockEngine {
            orden += it.method
            if (it.method == HttpMethod.Post) { posts++; delay(1000); respond(confirmado, headers = headers) }
            else respond(pagina(confirmado), headers = headers)
        })
        try {
            local.guardarPendiente(nuevo)
            val repo = ProductoRepositorioOffline(local, ProductoApi(cliente))
            awaitAll(async { repo.sincronizar() }, async { repo.sincronizar() })
            assertEquals(1, posts)
            assertEquals(listOf(HttpMethod.Post, HttpMethod.Get, HttpMethod.Get), orden)
            assertEquals(99L, local.listar().single().id)
            assertFalse(local.listar().single().pendiente)
        } finally { cliente.close(); driver.close() }
    }

    @Test fun el401NotificaLaPoliticaDeSesionYDetieneLosSiguientesEnvios() = runTest {
        val driver = crearDriverEnMemoria()
        val local = ProductoLocal(baseEnMemoria(driver))
        val manager = SesionManager()
        var peticiones = 0
        val cliente = crearHttpClient(baseUrl = "http://prueba/api/v1/", esDepuracion = false, engine = MockEngine {
            peticiones++; respond("{}", HttpStatusCode.Unauthorized, headers)
        })
        try {
            local.guardarPendiente(nuevo)
            local.guardarPendiente(nuevo.copy(nombre = "Segunda fila"))
            val evento = async(start = CoroutineStart.UNDISPATCHED) { manager.eventos.first() }
            ProductoRepositorioOffline(local, ProductoApi(cliente), manager).sincronizar()
            assertEquals(ErrorSesion.SesionExpirada, evento.await())
            assertEquals(1, peticiones)
            assertEquals(1, local.contarFallidas())
            assertEquals(1, local.contarPendientes())
            assertEquals(2, local.listar().size)
        } finally { cliente.close(); driver.close() }
    }

    @Test fun conciliarRespuestaInciertaValidaDatosYConfirmaSinNuevoPost() = runTest {
        val driver = crearDriverEnMemoria()
        val local = ProductoLocal(baseEnMemoria(driver))
        var coincide = false
        var consultas = 0
        val cliente = crearHttpClient(baseUrl = "http://prueba/api/v1/", esDepuracion = false, engine = MockEngine {
            assertEquals(HttpMethod.Get, it.method)
            consultas++
            respond(if (coincide) confirmado else confirmado.replace("5.5", "8.0"), headers = headers)
        })
        try {
            val fila = local.guardarPendiente(nuevo)
            local.marcarFallida(fila.id, ProductoRepositorioOffline.MOTIVO_INCIERTO)
            val repo = ProductoRepositorioOffline(local, ProductoApi(cliente))
            assertFailsWith<IllegalArgumentException> { repo.conciliarPendiente(fila.id, 99) }
            assertNotNull(local.porId(fila.id))
            coincide = true
            repo.conciliarPendiente(fila.id, 99)
            assertNull(local.porId(fila.id))
            assertEquals(99L, local.listar().single().id)
            assertEquals(0, local.contarFallidas())
            assertEquals(2, consultas)
        } finally { cliente.close(); driver.close() }
    }
}
