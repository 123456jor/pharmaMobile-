package pe.edu.upeu.pharmamobil.data.remote

import kotlinx.io.IOException
import pe.edu.upeu.pharmamobil.domain.error.ErrorApi
import pe.edu.upeu.pharmamobil.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobil.domain.model.ErrorSesion
import pe.edu.upeu.pharmamobil.domain.model.ErrorSesionException
import pe.edu.upeu.pharmamobil.domain.model.ResultadoEnvio

/** Solo se emite ANTES de llamar a POST: acredita que no se transmitió la creación. */
class EnvioNoIniciadoException : IOException("No se inició el envío: no hay red")

fun resultadoDeError(fallo: Throwable): ResultadoEnvio = when (val error = (fallo as? ErrorApiException)?.error) {
    ErrorApi.SinConexion -> ResultadoEnvio.Transitorio("Sin conexión con PharmaSoft")
    ErrorApi.TiempoAgotado -> ResultadoEnvio.Transitorio("Se agotó el tiempo de espera")
    ErrorApi.Servidor -> ResultadoEnvio.Transitorio("El servidor no pudo atender la solicitud")
    is ErrorApi.Validacion -> ResultadoEnvio.Permanente("Validación: " + error.porCampo.entries.joinToString { "${it.key}: ${it.value}" })
    ErrorApi.NoEncontrado -> ResultadoEnvio.Permanente("No se encontró el recurso")
    is ErrorApi.Conflicto -> ResultadoEnvio.Permanente("Conflicto: ${error.mensaje}")
    null -> when ((fallo as? ErrorSesionException)?.error) {
        ErrorSesion.CredencialesInvalidas, ErrorSesion.SesionExpirada -> ResultadoEnvio.Permanente("Sesión vencida")
        ErrorSesion.SinPermiso -> ResultadoEnvio.Permanente("No tienes permiso para enviar este producto")
        ErrorSesion.SinConexion -> ResultadoEnvio.Transitorio("Sin conexión con PharmaSoft")
        null -> ResultadoEnvio.Permanente("Resultado incierto. Revisa el servidor antes de volver a enviar")
    }
}

/** No confundir la clasificación transitoria con permiso para repetir un POST. */
fun resultadoPostIncierto(fallo: Throwable): Boolean =
    (fallo as? ErrorApiException)?.let {
        (it.error == ErrorApi.SinConexion || it.error == ErrorApi.TiempoAgotado) &&
            it.cause !is EnvioNoIniciadoException
    } ?: (fallo !is ErrorSesionException)
