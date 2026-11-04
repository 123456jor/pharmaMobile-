package pe.edu.upeu.pharmamobil.data.firebase

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.crashlytics.crashlytics
import dev.gitlive.firebase.messaging.messaging
import pe.edu.upeu.pharmamobil.domain.port.Telemetria

/** SDK real, instancia única en Koin. Ninguna excepción de telemetría interrumpe el catálogo. */
class TelemetriaFirebase : Telemetria {
    override fun registrarEvento(nombre: String, datos: Map<String, String>) {
        if (nombre !in EVENTOS_TECNICOS) return
        val seguro = contextoTecnicoSeguro(datos)
        try {
            Firebase.crashlytics.log("$nombre $seguro")
            println("TELEMETRIA_EVENTO nombre=$nombre contexto=$seguro sdk_llamado=true")
        } catch (_: Exception) { println("TELEMETRIA_FALLO operacion=evento") }
    }
    override fun registrarError(e: Throwable, contexto: Map<String, String>) {
        val seguro = contextoTecnicoSeguro(contexto)
        try {
            Firebase.crashlytics.recordException(ReporteTecnicoSeguro(seguro["tipo_error"] ?: "inesperado"), seguro)
            println("TELEMETRIA_NO_FATAL contexto=$seguro sdk_llamado=true")
        } catch (_: Exception) { println("TELEMETRIA_FALLO operacion=no_fatal") }
    }
    /** Comprueba Messaging sin imprimir, persistir ni exportar su token de dispositivo. */
    suspend fun comprobarMensajeria(): Boolean = Firebase.messaging.getToken().isNotBlank()
}
