package pe.edu.upeu.pharmamobil.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException
import pe.edu.upeu.pharmamobil.data.local.AlmacenSeguro
import pe.edu.upeu.pharmamobil.data.remote.dto.ErrorSesionDto
import pe.edu.upeu.pharmamobil.data.remote.dto.LoginRequestDto
import pe.edu.upeu.pharmamobil.data.remote.dto.LoginResponseDto
import pe.edu.upeu.pharmamobil.data.remote.dto.UsuarioActualDto
import pe.edu.upeu.pharmamobil.data.remote.dto.aSesion
import pe.edu.upeu.pharmamobil.data.remote.mapearErrorSesion
import pe.edu.upeu.pharmamobil.domain.model.Credenciales
import pe.edu.upeu.pharmamobil.domain.model.ErrorSesion
import pe.edu.upeu.pharmamobil.domain.model.ErrorSesionException
import pe.edu.upeu.pharmamobil.domain.model.Sesion
import pe.edu.upeu.pharmamobil.domain.repository.AuthRepository

class AuthRepositorioRest(
    private val cliente: HttpClient,
    private val almacen: AlmacenSeguro
) : AuthRepository {
    override suspend fun iniciarSesion(credenciales: Credenciales): Sesion = try {
        val dto: LoginResponseDto = cliente.post("auth/login") {
            setBody(LoginRequestDto(credenciales.usuario, credenciales.clave))
        }.body()
        almacen.guardarToken(dto.token)
        limpiarTokenEnCache()
        dto.aSesion()
    } catch (cancelacion: CancellationException) {
        throw cancelacion
    } catch (peticion: ClientRequestException) {
        val errorDto = try { peticion.response.body<ErrorSesionDto>() } catch (_: Exception) { null }
        throw ErrorSesionException(mapearErrorSesion(peticion.response.status, errorDto?.message), peticion)
    } catch (timeout: HttpRequestTimeoutException) {
        throw ErrorSesionException(ErrorSesion.SinConexion, timeout)
    } catch (timeout: io.ktor.client.network.sockets.ConnectTimeoutException) {
        throw ErrorSesionException(ErrorSesion.SinConexion, timeout)
    } catch (servidor: ServerResponseException) {
        throw ErrorSesionException(ErrorSesion.SinConexion, servidor)
    } catch (conexion: IOException) {
        throw ErrorSesionException(ErrorSesion.SinConexion, conexion)
    }

    override suspend fun sesionGuardada(): Sesion? {
        if (almacen.leerToken() == null) return null
        return try {
            val usuario: UsuarioActualDto = cliente.get("auth/me").body()
            Sesion(almacen.leerToken().orEmpty(), usuario.username, usuario.nombreCompleto, usuario.permisos.toSet())
        } catch (cancelacion: CancellationException) {
            throw cancelacion
        } catch (error: ErrorSesionException) {
            if (error.error == ErrorSesion.SesionExpirada || error.error == ErrorSesion.CredencialesInvalidas) {
                cerrarSesion()
                null
            } else throw error
        } catch (timeout: HttpRequestTimeoutException) {
            throw ErrorSesionException(ErrorSesion.SinConexion, timeout)
        } catch (timeout: io.ktor.client.network.sockets.ConnectTimeoutException) {
            throw ErrorSesionException(ErrorSesion.SinConexion, timeout)
        } catch (servidor: ServerResponseException) {
            throw ErrorSesionException(ErrorSesion.SinConexion, servidor)
        } catch (conexion: IOException) {
            throw ErrorSesionException(ErrorSesion.SinConexion, conexion)
        } catch (peticion: ClientRequestException) {
            if (peticion.response.status == HttpStatusCode.Unauthorized) {
                cerrarSesion()
                throw ErrorSesionException(ErrorSesion.SesionExpirada, peticion)
            }
            if (peticion.response.status == HttpStatusCode.Forbidden) {
                throw ErrorSesionException(ErrorSesion.SinPermiso, peticion)
            }
            throw peticion
        }
    }

    override suspend fun cerrarSesion() {
        almacen.borrarToken()
        limpiarTokenEnCache()
    }

    private fun limpiarTokenEnCache() {
        cliente.authProvider<BearerAuthProvider>()?.clearToken()
    }
}
