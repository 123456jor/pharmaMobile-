package pe.edu.upeu.pharmamobil.domain.platform

/** Doble del Compartidor: guarda lo que se pidio compartir en vez de abrir la hoja del sistema. */
class FakeCompartidor : Compartidor {
    val textos = mutableListOf<String>()
    override fun compartir(texto: String) { textos += texto }
}
