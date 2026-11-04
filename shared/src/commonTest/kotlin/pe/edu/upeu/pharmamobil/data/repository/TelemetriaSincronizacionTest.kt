package pe.edu.upeu.pharmamobil.data.repository

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobil.data.local.*
import pe.edu.upeu.pharmamobil.data.remote.*
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.port.FakeTelemetria
import kotlin.test.*

class TelemetriaSincronizacionTest {
    @Test fun laRutaRealUsaLaInterfazYEmiteInicioErrorYFinSinNombresNiCredenciales() = runTest {
        for (estado in listOf(HttpStatusCode.Conflict, HttpStatusCode.ServiceUnavailable)) {
            val driver = crearDriverEnMemoria()
            val local = ProductoLocal(baseEnMemoria(driver))
            val telemetria = FakeTelemetria()
            val cliente = crearHttpClient(baseUrl = "http://prueba/api/v1/", esDepuracion = false, engine = MockEngine {
                if (it.method == HttpMethod.Post) respond("""{"message":"nombre privado no exportable"}""", estado,
                    headersOf("Content-Type", "application/json"))
                else respond("""{"contenido":[],"pagina":0,"tamanio":20,"totalElementos":0,"totalPaginas":0,"ultima":true}""",
                    headers = headersOf("Content-Type", "application/json"))
            })
            try {
                local.guardarPendiente(Producto(0, "Nombre privado", 5.0, 2, categoriaId = 1))
                ProductoRepositorioOffline(local, ProductoApi(cliente), telemetria = telemetria).sincronizar()
                assertEquals(listOf("sync_inicio", if (estado == HttpStatusCode.Conflict) "error_permanente" else "error_transitorio", "sync_fin"),
                    telemetria.eventos.map { it.first })
                assertFalse(telemetria.eventos.toString().contains("privado", true))
                assertTrue(telemetria.eventos.all { it.second.keys.all { k -> k in setOf("pantalla", "operacion", "tipo_error", "codigo_estado", "enviadas", "fallidas", "pendientes") } })
            } finally { cliente.close(); driver.close() }
        }
    }
}
