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

suspend fun <T> ejecutarLlamada(bloque: suspend () -> T): T = try {
    bloque()
} catch (cancelacion: CancellationException) {
    throw cancelacion
} catch (timeout: HttpRequestTimeoutException) {
    throw ErrorApiException(ErrorApi.TiempoAgotado, timeout)
} catch (timeout: ConnectTimeoutException) {
    throw ErrorApiException(ErrorApi.TiempoAgotado, timeout)
} catch (peticion: ClientRequestException) {
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
