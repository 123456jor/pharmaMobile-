package pe.edu.upeu.pharmamobil.domain.port

class FakeTelemetria : Telemetria {
    val eventos = mutableListOf<Pair<String, Map<String, String>>>()
    val errores = mutableListOf<Map<String, String>>()
    override fun registrarEvento(nombre: String, datos: Map<String, String>) { eventos += nombre to datos }
    override fun registrarError(e: Throwable, contexto: Map<String, String>) { errores += contexto }
}
