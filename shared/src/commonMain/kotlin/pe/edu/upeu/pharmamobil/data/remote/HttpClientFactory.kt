package pe.edu.upeu.pharmamobil.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun crearHttpClient(
    requestTimeoutMillis: Long = TIEMPO_SOLICITUD_POR_DEFECTO_MS,
    baseUrl: String = plataformaApiBaseUrl,
    engineFactory: HttpClientEngineFactory<*> = plataformaHttpEngine,
    engine: HttpClientEngine? = null
): HttpClient {
    val configurar: HttpClientConfig<*>.() -> Unit = {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
    install(Logging) {
        logger = object : Logger {
            override fun log(message: String) = println(message)
        }
        level = LogLevel.HEADERS
    }
    install(HttpTimeout) {
        this.requestTimeoutMillis = requestTimeoutMillis
    }
    // Si expectSuccess fuera false, las respuestas 4xx no lanzarían
    // ClientRequestException y no llegarían al traductor ErrorApi.
    expectSuccess = true
    defaultRequest {
        url(baseUrl)
    }
    }
    return if (engine == null) HttpClient(engineFactory, configurar) else HttpClient(engine, configurar)
}

const val TIEMPO_SOLICITUD_POR_DEFECTO_MS = 30_000L
