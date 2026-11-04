package pe.edu.upeu.pharmamobil.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import pe.edu.upeu.pharmamobil.domain.port.Conectividad

/** Una instancia de aplicación: nunca conserva una Activity. */
class ConectividadAndroid(contexto: Context) : Conectividad {
    private val gestor = contexto.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private fun disponible(): Boolean = gestor.getNetworkCapabilities(gestor.activeNetwork)
        ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    private val estado = MutableStateFlow(disponible())
    override val conectado = estado.asStateFlow()
    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) { estado.value = disponible() }
        override fun onLost(network: Network) { estado.value = disponible() }
        override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
            estado.value = disponible()
        }
    }
    init { gestor.registerDefaultNetworkCallback(callback) }
}
