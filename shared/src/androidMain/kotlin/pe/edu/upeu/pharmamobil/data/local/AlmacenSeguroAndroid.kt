package pe.edu.upeu.pharmamobil.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import pe.edu.upeu.pharmamobil.domain.model.Sesion

class AlmacenSeguroAndroid(contexto: Context) : AlmacenSeguro {
    private val llave = MasterKey.Builder(contexto)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    private val preferencias = EncryptedSharedPreferences.create(
        contexto,
        "sesion_segura",
        llave,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    override fun guardarToken(token: String) { preferencias.edit().putString(CLAVE, token).apply() }
    override fun leerToken(): String? = preferencias.getString(CLAVE, null)
    override fun guardarSesion(sesion: Sesion) { preferencias.edit().putString("sesion", Json.encodeToString(sesion)).apply() }
    override fun leerSesion(): Sesion? = preferencias.getString("sesion", null)?.let {
        try { Json.decodeFromString<Sesion>(it) } catch (_: Exception) { null }
    }
    override fun borrarToken() { preferencias.edit().remove(CLAVE).remove("sesion").apply() }

    private companion object { const val CLAVE = "jwt" }
}
