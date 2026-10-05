package pe.edu.upeu.pharmamobil.data.remote

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.cio.CIO

actual val plataformaApiBaseUrl: String = "http://10.0.2.2:8080/api/v1/"
actual val plataformaHttpEngine: HttpClientEngineFactory<*> = CIO
