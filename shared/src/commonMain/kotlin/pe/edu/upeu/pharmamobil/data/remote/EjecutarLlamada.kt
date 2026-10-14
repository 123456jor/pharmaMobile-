package pe.edu.upeu.pharmamobil.data.remote

import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException
import pe.edu.upeu.pharmamobil.data.remote.dto.ErrorResponseDto
import pe.edu.upeu.pharmamobil.domain.error.ErrorApi
import pe.edu.upeu.pharmamobil.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobil.domain.auth.SesionManager
import pe.edu.upeu.pharmamobil.domain.model.ErrorSesionException

suspend fun <T> ejecutarLlamada(sesionManager: SesionManager? = null, bloque: suspend () -> T): T = try {
    bloque()
} catch (cancelacion: CancellationException) {
    throw cancelacion
} catch (sesion: ErrorSesionException) {
    throw sesion
} catch (timeout: HttpRequestTimeoutException) {
    throw ErrorApiException(ErrorApi.TiempoAgotado, timeout)
} catch (timeout: ConnectTimeoutException) {
    throw ErrorApiException(ErrorApi.TiempoAgotado, timeout)
} catch (peticion: ClientRequestException) {
    if (peticion.response.status == HttpStatusCode.Unauthorized || peticion.response.status == HttpStatusCode.Forbidden) {
        val dto = try { peticion.response.body<pe.edu.upeu.pharmamobil.data.remote.dto.ErrorSesionDto>() } catch (_: Exception) { null }
        val mapeado = mapearErrorSesion(peticion.response.status, dto?.message)
        val error = if (peticion.response.status == HttpStatusCode.Unauthorized && mapeado == pe.edu.upeu.pharmamobil.domain.model.ErrorSesion.CredencialesInvalidas)
            pe.edu.upeu.pharmamobil.domain.model.ErrorSesion.SesionExpirada else mapeado
        sesionManager?.notificar(error)
        throw ErrorSesionException(error, peticion)
    }
    throw ErrorApiException(peticion.traducirCliente(), peticion)
} catch (servidor: ServerResponseException) {
    throw ErrorApiException(ErrorApi.Servidor, servidor)
} catch (conexion: IOException) {
    throw ErrorApiException(ErrorApi.SinConexion, conexion)
}

private suspend fun ClientRequestException.traducirCliente(): ErrorApi {
    val cuerpo = try {
        response.body<ErrorResponseDto>()
    } catch (cancelacion: CancellationException) {
        throw cancelacion
    } catch (_: Exception) {
        null
    }

    return when (response.status) {
        HttpStatusCode.BadRequest -> ErrorApi.Validacion(cuerpo?.validationErrors.orEmpty())
        HttpStatusCode.NotFound -> ErrorApi.NoEncontrado
        HttpStatusCode.Conflict -> ErrorApi.Conflicto(cuerpo?.message ?: "Operación no permitida")
        else -> ErrorApi.Servidor
    }
}
