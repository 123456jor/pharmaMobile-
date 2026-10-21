package pe.edu.upeu.pharmamobil.domain.model

data class Credenciales(val usuario: String, val clave: String)

@kotlinx.serialization.Serializable
data class Sesion(
    val token: String,
    val usuario: String,
    val nombreCompleto: String,
    val permisos: Set<String>,
    val expiracionEpochSegundos: Long = 0
) {
    fun puede(permiso: String): Boolean = permiso in permisos
}

sealed interface ErrorSesion {
    data object CredencialesInvalidas : ErrorSesion
    data object SesionExpirada : ErrorSesion
    data object SinPermiso : ErrorSesion
    data object SinConexion : ErrorSesion
}

class ErrorSesionException(val error: ErrorSesion, cause: Throwable? = null) :
    Exception("Error de sesión: $error", cause)
