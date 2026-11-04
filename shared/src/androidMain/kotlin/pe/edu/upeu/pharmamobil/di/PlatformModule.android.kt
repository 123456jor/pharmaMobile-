package pe.edu.upeu.pharmamobil.di

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module
import pe.edu.upeu.pharmamobil.domain.platform.Compartidor
import pe.edu.upeu.pharmamobil.platform.CompartidorAndroid
import pe.edu.upeu.pharmamobil.data.local.AlmacenSeguro
import pe.edu.upeu.pharmamobil.data.local.AlmacenSeguroAndroid
import pe.edu.upeu.pharmamobil.data.local.CreadorDeDriver
import pe.edu.upeu.pharmamobil.data.local.CreadorDeDriverAndroid

actual val platformModule: Module = module {
    single { pe.edu.upeu.pharmamobil.data.permiso.GestorPermisosAndroid(androidContext(),
        androidContext().getSharedPreferences("permisos", android.content.Context.MODE_PRIVATE)) }
    single<pe.edu.upeu.pharmamobil.domain.permiso.GestorPermisos> { get<pe.edu.upeu.pharmamobil.data.permiso.GestorPermisosAndroid>() }
    single<pe.edu.upeu.pharmamobil.domain.port.AvisadorSincronizacion> {
        pe.edu.upeu.pharmamobil.data.permiso.AvisadorSincronizacionAndroid(androidContext())
    }
    single<pe.edu.upeu.pharmamobil.domain.port.Conectividad> {
        pe.edu.upeu.pharmamobil.data.network.ConectividadAndroid(androidContext())
    }
    single<CreadorDeDriver> { CreadorDeDriverAndroid(androidContext()) }
    single<Compartidor> { CompartidorAndroid(androidContext()) }
    single<AlmacenSeguro> { AlmacenSeguroAndroid(androidContext()) }
}
