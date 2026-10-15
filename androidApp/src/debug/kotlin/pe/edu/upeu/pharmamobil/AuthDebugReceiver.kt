package pe.edu.upeu.pharmamobil

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.koin.core.context.GlobalContext
import pe.edu.upeu.pharmamobil.data.local.AlmacenSeguroAndroid

/** Herramienta disponible solo en el APK debug para provocar un JWT inválido. */
class AuthDebugReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACCION_PETICION_PROTEGIDA) {
            val pendiente = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val cliente = GlobalContext.get().get<HttpClient>()
                    val respuesta = try {
                        cliente.get("productos")
                    } catch (error: ClientRequestException) {
                        error.response
                    }
                    val mensaje = runCatching {
                        Json.parseToJsonElement(respuesta.bodyAsText()).jsonObject["message"]?.jsonPrimitive?.content
                    }.getOrNull()
                    Log.i(TAG, "PETICION_PROTEGIDA HTTP=${respuesta.status.value} message=${mensaje ?: "sin error"}")
                    Log.i(TAG, "ALMACEN token_presente=${AlmacenSeguroAndroid(context.applicationContext).leerToken() != null}")
                } catch (error: Exception) {
                    Log.e(TAG, "Prueba protegida falló: ${error.javaClass.simpleName}")
                } finally {
                    pendiente.finish()
                }
            }
            return
        }
        if (intent.action != ACCION_ALTERAR_TOKEN) return

        val almacen = AlmacenSeguroAndroid(context.applicationContext)
        val token = almacen.leerToken()
        if (token.isNullOrBlank()) {
            Log.w(TAG, "No existe un token para alterar")
            return
        }

        val inicioFirma = token.lastIndexOf('.') + 1
        if (inicioFirma !in token.indices) {
            Log.w(TAG, "El valor guardado no tiene formato JWT")
            return
        }

        val alterado = token.toCharArray().also { caracteres ->
            caracteres[inicioFirma] = if (caracteres[inicioFirma] == 'A') 'B' else 'A'
        }.concatToString()

        almacen.guardarToken(alterado)
        Log.i(TAG, "Token JWT alterado para la siguiente ejecución; contenido omitido")
    }

    private companion object {
        const val TAG = "AuthDebugReceiver"
        const val ACCION_ALTERAR_TOKEN = "pe.edu.upeu.pharmamobil.DEBUG_ALTERAR_TOKEN"
        const val ACCION_PETICION_PROTEGIDA = "pe.edu.upeu.pharmamobil.DEBUG_PETICION_PROTEGIDA"
    }
}
