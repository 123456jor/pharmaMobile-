package pe.edu.upeu.pharmamobil.data.permiso

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference
import pe.edu.upeu.pharmamobil.domain.permiso.*

fun nombreDeSistema(permiso: Permiso): String? = when (permiso) {
    Permiso.CAMARA -> Manifest.permission.CAMERA
    Permiso.UBICACION -> Manifest.permission.ACCESS_FINE_LOCATION
    Permiso.NOTIFICACIONES -> if (Build.VERSION.SDK_INT >= 33) Manifest.permission.POST_NOTIFICATIONS else null
}

/** Registrar en onCreate antes de STARTED. Se cancela al destruir la Activity. */
class PuentePermisos(actividad: ComponentActivity, private val alDestruir: (PuentePermisos) -> Unit) : DefaultLifecycleObserver {
    private val referencia = WeakReference(actividad)
    private var resultado: CompletableDeferred<Boolean>? = null
    private val launcher = actividad.registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        resultado?.complete(it)
    }
    init {
        actividad.lifecycle.addObserver(this)
    }
    fun explicar(nombre: String): Boolean? = referencia.get()?.shouldShowRequestPermissionRationale(nombre)
    suspend fun solicitar(nombre: String): Boolean {
        check(resultado == null) { "Ya hay una solicitud de permiso activa" }
        if (referencia.get() == null) throw CancellationException("La actividad ya no está disponible")
        val espera = CompletableDeferred<Boolean>()
        resultado = espera
        try { launcher.launch(nombre); return espera.await() }
        finally { resultado = null }
    }
    override fun onDestroy(owner: LifecycleOwner) {
        resultado?.cancel(CancellationException("Actividad destruida durante la solicitud"))
        resultado = null
        launcher.unregister()
        referencia.clear()
        alDestruir(this)
        owner.lifecycle.removeObserver(this)
    }
}

class GestorPermisosAndroid(contexto: Context, private val preferencias: SharedPreferences) : GestorPermisos {
    private val contexto = contexto.applicationContext
    var puente: PuentePermisos? = null
        private set
    private val solicitud = Mutex()

    fun vincular(actividad: ComponentActivity) {
        puente = PuentePermisos(actividad) { destruido -> if (puente === destruido) puente = null }
    }
    override suspend fun estado(permiso: Permiso): EstadoPermiso = withContext(Dispatchers.Main.immediate) {
        val nombre = nombreDeSistema(permiso) ?: return@withContext EstadoPermiso.Concedido
        val concedido = contexto.checkSelfPermission(nombre) == PackageManager.PERMISSION_GRANTED
        val pedido = preferencias.getBoolean("pedido_$permiso", false)
        val explicar = puente?.explicar(nombre) ?: return@withContext if (concedido)
            EstadoPermiso.Concedido else EstadoPermiso.Denegado
        resolverEstadoPermiso(concedido, pedido, explicar)
    }
    override suspend fun solicitar(permiso: Permiso): EstadoPermiso = solicitud.withLock {
        withContext(Dispatchers.Main.immediate) {
            val nombre = nombreDeSistema(permiso) ?: return@withContext EstadoPermiso.Concedido
            val actual = estado(permiso)
            if (actual != EstadoPermiso.Denegado) return@withContext actual
            val actividad = puente ?: throw CancellationException("No hay actividad para solicitar permiso")
            preferencias.edit().putBoolean("pedido_$permiso", true).apply()
            actividad.solicitar(nombre)
            estado(permiso)
        }
    }
    override fun abrirAjustes() {
        contexto.startActivity(Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:${contexto.packageName}")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
