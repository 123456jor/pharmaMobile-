package pe.edu.upeu.pharmamobil.domain.model

sealed interface ResultadoEnvio {
    data class Exito(val idServidor: Long) : ResultadoEnvio
    data class Transitorio(val motivo: String) : ResultadoEnvio
    data class Permanente(val motivo: String) : ResultadoEnvio
}

data class ResumenSync(val enviadas: Int, val fallidas: Int, val pendientes: Int)

/** El índice es el número de fallos ANTES del nuevo intento: 0 → 1 segundo. */
fun esperaEntreIntentos(intentosAnteriores: Int): Long = 1_000L shl intentosAnteriores.coerceIn(0, 4)

/** Motivos persistidos que gobiernan las acciones, sin depender de clases de datos. */
object PoliticaEnvio {
    const val INCIERTO = "Resultado incierto: revisa PharmaSoft. Si ya existe, concilia su ID; reenvía solo si confirmas que no llegó."
    val recuperables = setOf("Sin conexión con PharmaSoft", "Se agotó el tiempo de espera",
        "El servidor no pudo atender la solicitud")
}
