package pe.edu.upeu.pharmamobil.presentation.producto

data class SincronizacionUiState(
    val enCurso: Boolean = false,
    val pendientes: Int = 0,
    val fallidas: Int = 0,
    val ultimaSync: Long? = null,
    val conectado: Boolean = false
)
