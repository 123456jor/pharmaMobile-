package pe.edu.upeu.pharmamobil

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import pe.edu.upeu.pharmamobil.data.local.AlmacenSeguroAndroid

/** Herramienta disponible solo en el APK debug para provocar un JWT inválido. */
class AuthDebugReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
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
    }
}
