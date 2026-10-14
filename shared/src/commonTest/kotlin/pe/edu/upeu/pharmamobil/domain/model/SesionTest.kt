package pe.edu.upeu.pharmamobil.domain.model

import io.ktor.http.HttpStatusCode
import pe.edu.upeu.pharmamobil.data.remote.mapearErrorSesion
import kotlin.test.Test
import kotlin.test.assertEquals

class SesionTest {
    @Test
    fun permisoSeConsultaPorAutoridad() {
        val sesion = Sesion("token", "almacen", "Almacén", setOf("CATALOGO_LEER", "CATALOGO_ESCRIBIR"))
        assertEquals(true, sesion.puede("CATALOGO_ESCRIBIR"))
        assertEquals(false, sesion.puede("CLIENTES_LEER"))
    }

    @Test
    fun traduceEstadosDeSesion() {
        assertEquals(ErrorSesion.SesionExpirada, mapearErrorSesion(HttpStatusCode.Unauthorized, "El token ha expirado"))
        assertEquals(ErrorSesion.CredencialesInvalidas, mapearErrorSesion(HttpStatusCode.Unauthorized, "El token no es válido"))
        assertEquals(ErrorSesion.SinPermiso, mapearErrorSesion(HttpStatusCode.Forbidden, "No tiene permisos"))
    }
}
