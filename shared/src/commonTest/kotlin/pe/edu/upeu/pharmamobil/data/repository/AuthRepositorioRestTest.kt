package pe.edu.upeu.pharmamobil.data.repository

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobil.data.local.AlmacenSeguro
import pe.edu.upeu.pharmamobil.data.remote.crearHttpClient
import pe.edu.upeu.pharmamobil.domain.model.Credenciales
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import pe.edu.upeu.pharmamobil.domain.model.ErrorSesion
import pe.edu.upeu.pharmamobil.domain.model.ErrorSesionException

class AuthRepositorioRestTest {
    private class AlmacenMemoria : AlmacenSeguro {
        var token: String? = null
        override fun guardarToken(token: String) { this.token = token }
        override fun leerToken(): String? = token
        override fun borrarToken() { token = null }
    }

    @Test
    fun guardaTokenTrasLoginYLoUsaParaReconstruirSesion() = runTest {
        val almacen = AlmacenMemoria()
        val authPorLlamada = mutableListOf<String?>()
        var llamada = 0
        val engine = MockEngine { request ->
            authPorLlamada += request.headers[HttpHeaders.Authorization]
            val body = if (llamada++ == 0) {
                """{"token":"jwt-seguro","tipo":"Bearer","expiraEnSegundos":3600,"username":"admin","nombreCompleto":"Administradora","roles":["ADMIN"],"permisos":["CATALOGO_LEER","CATALOGO_ESCRIBIR"]}"""
            } else {
                """{"username":"admin","empleadoNombre":"Administradora","roles":["ADMIN"],"permisos":["CATALOGO_LEER","CATALOGO_ESCRIBIR"]}"""
            }
            respond(body, headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
        }
        val cliente = crearHttpClient(baseUrl = "http://test/api/v1/", engine = engine, almacen = almacen, esDepuracion = false)
        try {
            val repositorio = AuthRepositorioRest(cliente, almacen)
            val login = repositorio.iniciarSesion(Credenciales("admin", "clave"))
            val restaurada = repositorio.sesionGuardada()
            assertEquals("jwt-seguro", login.token)
            assertEquals(login, restaurada)
            repositorio.cerrarSesion()
            cliente.get("productos")
            assertEquals(listOf(null, "Bearer jwt-seguro", null), authPorLlamada)
            assertEquals(null, almacen.token)
        } finally { cliente.close() }
    }

    @Test
    fun login401SeExponeComoCredencialesInvalidasSinGuardarToken() = runTest {
        val almacen = AlmacenMemoria()
        val engine = MockEngine {
            respond(
                """{"status":401,"error":"Unauthorized","message":"Credenciales incorrectas","path":"/api/v1/auth/login"}""",
                status = HttpStatusCode.Unauthorized,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        val cliente = crearHttpClient(baseUrl = "http://test/api/v1/", engine = engine, almacen = almacen, esDepuracion = false)
        try {
            val fallo = assertFailsWith<ErrorSesionException> {
                AuthRepositorioRest(cliente, almacen).iniciarSesion(Credenciales("admin", "incorrecta"))
            }
            assertEquals(ErrorSesion.CredencialesInvalidas, fallo.error)
            assertEquals(null, almacen.leerToken())
        } finally { cliente.close() }
    }
}
