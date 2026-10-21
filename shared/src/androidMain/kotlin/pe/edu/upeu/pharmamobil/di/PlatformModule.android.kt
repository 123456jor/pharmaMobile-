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
    single<CreadorDeDriver> { CreadorDeDriverAndroid(androidContext()) }
    single<Compartidor> { CompartidorAndroid(androidContext()) }
    single<AlmacenSeguro> { AlmacenSeguroAndroid(androidContext()) }
}
