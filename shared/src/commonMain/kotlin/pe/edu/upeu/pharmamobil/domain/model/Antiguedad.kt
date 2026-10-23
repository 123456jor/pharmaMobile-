package pe.edu.upeu.pharmamobil.domain.model

/** Recibe el reloj como dato: permite comprobar los límites sin esperar tiempo real. */
fun formatearAntiguedad(ahora: Long, ultima: Long?): String {
    if (ultima == null) return "Aún no se ha sincronizado"
    val minutos = ((ahora - ultima).coerceAtLeast(0) / 60_000)
    return when {
        minutos == 0L -> "Actualizado hace unos segundos"
        minutos < 60 -> "Actualizado hace $minutos ${if (minutos == 1L) "minuto" else "minutos"}"
        minutos < 1440 -> (minutos / 60).let { "Actualizado hace $it ${if (it == 1L) "hora" else "horas"}" }
        else -> (minutos / 1440).let { "Actualizado hace $it ${if (it == 1L) "día" else "días"}" }
    }
}
