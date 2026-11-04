package pe.edu.upeu.pharmamobil.data.firebase

import kotlin.test.*

class PrivacidadTelemetriaTest {
    @Test fun excluyeCredencialesDatosPersonalesYValoresNoPermitidos() {
        val datos = mapOf("pantalla" to "productos", "operacion" to "subir", "tipo_error" to "validacion",
            "codigo_estado" to "400", "Authorization" to "Bearer secreto", "jwt" to "secreto",
            "password" to "clave", "usuario" to "Jorge", "nombre" to "Producto de prueba",
            "message" to "cuerpo privado", "enviadas" to "3", "fallidas" to "usuario privado")
        assertEquals(mapOf("pantalla" to "productos", "operacion" to "subir", "tipo_error" to "validacion",
            "codigo_estado" to "400", "enviadas" to "3"), contextoTecnicoSeguro(datos))
        assertTrue(contextoTecnicoSeguro(mapOf("pantalla" to "mi usuario", "operacion" to "mi producto",
            "tipo_error" to "Bearer secreto", "codigo_estado" to "401 token")).isEmpty())
    }
    @Test fun excepcionSanitizadaNoConservaMensajeCausaNiPilaOriginal() {
        val original = IllegalStateException("password=privado", Exception("jwt=privado"))
        val segura = ReporteTecnicoSeguro("controlado")
        assertNull(segura.cause)
        assertFalse(segura.stackTraceToString().contains(original.message!!))
        assertEquals("Fallo técnico: controlado", segura.message)
    }
}
