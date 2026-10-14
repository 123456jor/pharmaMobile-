package pe.edu.upeu.pharmamobil.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import pe.edu.upeu.pharmamobil.data.local.AlmacenSeguro
import pe.edu.upeu.pharmamobil.domain.auth.SesionManager

fun crearHttpClient(
    requestTimeoutMillis: Long = TIEMPO_SOLICITUD_POR_DEFECTO_MS,
    baseUrl: String = plataformaApiBaseUrl,
    engineFactory: HttpClientEngineFactory<*> = plataformaHttpEngine,
    engine: HttpClientEngine? = null,
    almacen: AlmacenSeguro? = null,
    sesionManager: SesionManager? = null,
    esDepuracion: Boolean = true
): HttpClient {
    val configurar: HttpClientConfig<*>.() -> Unit = {
        expectSuccess = true
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        install(Auth) {
            bearer {
                loadTokens { almacen?.leerToken()?.let { BearerTokens(it, "") } }
                sendWithoutRequest { request -> !request.url.toString().substringBefore('?').endsWith("/auth/login") }
            }
        }
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) = println(message)
            }
            level = if (esDepuracion) LogLevel.HEADERS else LogLevel.NONE
            sanitizeHeader { it.equals(HttpHeaders.Authorization, ignoreCase = true) }
        }
        install(HttpTimeout) { this.requestTimeoutMillis = requestTimeoutMillis }
        defaultRequest {
            url(baseUrl)
            contentType(ContentType.Application.Json)
        }
    }
    return if (engine == null) HttpClient(engineFactory, configurar) else HttpClient(engine, configurar)
}

const val TIEMPO_SOLICITUD_POR_DEFECTO_MS = 30_000L
