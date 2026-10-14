package pe.edu.upeu.pharmamobil.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import pe.edu.upeu.pharmamobil.data.repository.ClienteRepositorioEnMemoria
import pe.edu.upeu.pharmamobil.data.repository.ProductoRepositorioRest
import pe.edu.upeu.pharmamobil.data.remote.ProductoApi
import pe.edu.upeu.pharmamobil.data.remote.crearHttpClient
import pe.edu.upeu.pharmamobil.data.remote.TIEMPO_SOLICITUD_POR_DEFECTO_MS
import pe.edu.upeu.pharmamobil.data.local.AlmacenSeguro
import pe.edu.upeu.pharmamobil.data.repository.AuthRepositorioRest
import pe.edu.upeu.pharmamobil.domain.auth.SesionManager
import pe.edu.upeu.pharmamobil.domain.repository.AuthRepository
import pe.edu.upeu.pharmamobil.domain.usecase.IniciarSesionUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ObtenerSesionGuardadaUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.CerrarSesionUseCase
import pe.edu.upeu.pharmamobil.presentation.sesion.SesionViewModel
import pe.edu.upeu.pharmamobil.domain.repository.ClienteRepository
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobil.domain.usecase.ListarClientesUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ListarProductosUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ActualizarProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.EliminarProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ObtenerProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.RegistrarClienteUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.RegistrarProductoUseCase
import pe.edu.upeu.pharmamobil.presentation.cliente.ClienteViewModel
import pe.edu.upeu.pharmamobil.presentation.producto.ProductoViewModel


val dataModule = module {
    single { SesionManager() }
    single {
        crearHttpClient(
            requestTimeoutMillis = if (MODO_TIMEOUT_PARA_PRUEBAS) 1L else TIEMPO_SOLICITUD_POR_DEFECTO_MS,
            almacen = get(),
            sesionManager = get(),
            esDepuracion = compilacionDepuracion
        )
    }
    single { ProductoApi(get()) }
    single<ProductoRepository> { ProductoRepositorioRest(get(), get()) }
    single<AuthRepository> { AuthRepositorioRest(get(), get()) }
    single<ClienteRepository> { ClienteRepositorioEnMemoria() }
}

/** Activar temporalmente para el escenario de timeout y volver a false al terminar. */
private const val MODO_TIMEOUT_PARA_PRUEBAS = false

val domainModule = module {
    factory { IniciarSesionUseCase(get()) }
    factory { ObtenerSesionGuardadaUseCase(get()) }
    factory { CerrarSesionUseCase(get()) }
    factory { RegistrarProductoUseCase(get()) }
    factory { ListarProductosUseCase(get()) }
    factory { ObtenerProductoUseCase(get()) }
    factory { ActualizarProductoUseCase(get()) }
    factory { EliminarProductoUseCase(get()) }
    factory { RegistrarClienteUseCase(get()) }
    factory { ListarClientesUseCase(get()) }
}

val presentationModule = module {
    viewModel { SesionViewModel(get(), get(), get(), get()) }
    viewModel { ProductoViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { ClienteViewModel(get(), get()) }
}


expect val platformModule: Module

private var compilacionDepuracion = false

fun initKoin(esDepuracion: Boolean = false, configuracionAdicional: KoinApplication.() -> Unit = {}) {
    compilacionDepuracion = esDepuracion
    startKoin {
        configuracionAdicional()
        modules(
            dataModule,
            domainModule,
            presentationModule,
            platformModule
        )
    }
}
