package pe.edu.upeu.pharmamobil.presentation.sesion

import pe.edu.upeu.pharmamobil.domain.model.Sesion

data class SesionUiState(
    val usuario: String = "",
    val clave: String = "",
    val cargando: Boolean = false,
    val error: String? = null,
    val sesion: Sesion? = null,
    val aviso: String? = null,
    val consultaLocal: Boolean = false,
    val sesionLocalExpirada: Boolean = false
)
