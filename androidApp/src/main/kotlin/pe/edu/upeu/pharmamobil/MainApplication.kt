package pe.edu.upeu.pharmamobil

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import pe.edu.upeu.pharmamobil.di.initKoin

/**
 * Arranca Koin una sola vez, antes que cualquier Activity. Se declara en el
 * manifiesto con android:name=".MainApplication".
 */
class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // shared lee esta propiedad al construir el cliente HTTP; permite que
        // los APK físicos usen el host LAN sin fijar la IP del equipo en Git.
        System.setProperty("pe.edu.upeu.pharmamobil.apiBaseUrl", BuildConfig.PHARMASOFT_API_BASE_URL)

        initKoin(esDepuracion = BuildConfig.DEBUG) {
            androidLogger()
            androidContext(this@MainApplication)
        }
    }
}
