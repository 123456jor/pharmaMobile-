package pe.edu.upeu.pharmamobil.data.firebase

/** Lista cerrada: nunca pasa nombres de productos, mensajes HTTP, usuarios ni tokens. */
fun contextoTecnicoSeguro(datos: Map<String, String>): Map<String, String> = datos.filter { (clave, valor) ->
    when (clave) {
        "pantalla" -> valor in setOf("productos", "sesion", "diagnostico")
        "operacion" -> valor in setOf("sincronizar", "subir", "descargar", "prueba_no_fatal", "iniciar_app", "conciliar")
        "tipo_error" -> valor in setOf("validacion", "conflicto", "servidor", "sin_conexion", "tiempo_agotado",
            "no_autorizado", "sin_privilegio", "no_encontrado", "inesperado", "controlado")
        "codigo_estado" -> valor in setOf("400", "401", "403", "404", "409", "500", "502", "503", "504")
        "enviadas", "fallidas", "pendientes" -> valor.toIntOrNull()?.let { it in 0..100000 } == true
        else -> false
    }
}

val EVENTOS_TECNICOS = setOf("sync_inicio", "sync_fin", "error_transitorio", "error_permanente",
    "error_inesperado", "prueba_no_fatal")

/** No conserva mensaje, causa ni pila del error original: podrían contener datos personales. */
class ReporteTecnicoSeguro(tipo: String) : Exception("Fallo técnico: $tipo")
