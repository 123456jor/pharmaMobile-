package pe.edu.upeu.pharmamobil.domain.model

sealed interface ResultadoEnvio {
    data class Exito(val idServidor: Long) : ResultadoEnvio
    data class Transitorio(val motivo: String) : ResultadoEnvio
    data class Permanente(val motivo: String) : ResultadoEnvio
}

data class ResumenSync(val enviadas: Int, val fallidas: Int, val pendientes: Int)

/** El índice es el número de fallos ANTES del nuevo intento: 0 → 1 segundo. */
fun esperaEntreIntentos(intentosAnteriores: Int): Long = 1_000L shl intentosAnteriores.coerceIn(0, 4)
