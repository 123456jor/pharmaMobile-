package pe.edu.upeu.pharmamobil.data.remote

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.darwin.Darwin

actual val plataformaApiBaseUrl: String = "http://localhost:8080/api/v1/"
actual val plataformaHttpEngine: HttpClientEngineFactory<*> = Darwin
