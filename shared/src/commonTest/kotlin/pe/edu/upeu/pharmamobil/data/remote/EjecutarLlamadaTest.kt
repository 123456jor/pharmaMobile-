package pe.edu.upeu.pharmamobil.data.remote

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import pe.edu.upeu.pharmamobil.domain.error.ErrorApi
import pe.edu.upeu.pharmamobil.domain.error.ErrorApiException
import kotlin.test.Test
import kotlin.test.assertEquals

class EjecutarLlamadaTest {
    private suspend fun verificarError(
        status: HttpStatusCode,
        json: String
    ): ErrorApiException {
        val engine = MockEngine {
            respond(
                content = json,
                status = status,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        val client = crearHttpClient(baseUrl = "http://test/api/v1/", engine = engine)
        return try {
            ejecutarLlamada { client.get("productos/99") }
            error("Se esperaba un ErrorApiException")
        } catch (fallo: ErrorApiException) {
            fallo
        } finally {
            client.close()
        }
    }

    @Test
    fun traduce404ANoEncontrado() = kotlinx.coroutines.test.runTest {
        assertEquals(ErrorApi.NoEncontrado, verificarError(HttpStatusCode.NotFound, "{} ").error)
    }

    @Test
    fun traduce409AlMensajeDelServidor() = kotlinx.coroutines.test.runTest {
        assertEquals(
            ErrorApi.Conflicto("El producto tiene ventas asociadas"),
            verificarError(HttpStatusCode.Conflict, """{"message":"El producto tiene ventas asociadas"}""").error
        )
    }

    @Test
    fun traduce400ConLosErroresPorCampo() = kotlinx.coroutines.test.runTest {
        assertEquals(
            ErrorApi.Validacion(mapOf("precio" to "Debe ser mayor o igual que 0.01")),
            verificarError(HttpStatusCode.BadRequest, """{"validationErrors":{"precio":"Debe ser mayor o igual que 0.01"}}""").error
        )
    }
}
