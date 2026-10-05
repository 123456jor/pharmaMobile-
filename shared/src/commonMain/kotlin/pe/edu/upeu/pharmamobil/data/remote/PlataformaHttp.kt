package pe.edu.upeu.pharmamobil.data.remote

import io.ktor.client.engine.HttpClientEngineFactory

expect val plataformaApiBaseUrl: String
expect val plataformaHttpEngine: HttpClientEngineFactory<*>
