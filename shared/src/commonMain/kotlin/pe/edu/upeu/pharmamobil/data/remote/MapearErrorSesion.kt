package pe.edu.upeu.pharmamobil.data.remote

import io.ktor.http.HttpStatusCode
import pe.edu.upeu.pharmamobil.domain.model.ErrorSesion

fun mapearErrorSesion(estado: HttpStatusCode, mensaje: String?): ErrorSesion = when (estado) {
    HttpStatusCode.Unauthorized -> if (mensaje?.contains("expirado", ignoreCase = true) == true)
        ErrorSesion.SesionExpirada else ErrorSesion.CredencialesInvalidas
    HttpStatusCode.Forbidden -> ErrorSesion.SinPermiso
    else -> ErrorSesion.SinConexion
}
