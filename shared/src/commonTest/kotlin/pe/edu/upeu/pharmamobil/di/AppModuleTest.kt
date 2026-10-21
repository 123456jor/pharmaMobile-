package pe.edu.upeu.pharmamobil.di

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import pe.edu.upeu.pharmamobil.data.repository.ClienteRepositorioEnMemoria
import pe.edu.upeu.pharmamobil.data.local.CreadorDeDriver
import pe.edu.upeu.pharmamobil.data.local.crearDriverEnMemoria
import pe.edu.upeu.pharmamobil.db.PharmaDb
import pe.edu.upeu.pharmamobil.data.local.AlmacenSeguro
import pe.edu.upeu.pharmamobil.data.repository.ProductoRepositorioOffline
import pe.edu.upeu.pharmamobil.data.repository.FakeProductoRepository
import pe.edu.upeu.pharmamobil.domain.platform.Compartidor
import pe.edu.upeu.pharmamobil.domain.platform.FakeCompartidor
import pe.edu.upeu.pharmamobil.domain.repository.ClienteRepository
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobil.domain.usecase.ListarClientesUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ListarProductosUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.RegistrarClienteUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.RegistrarProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ActualizarProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.EliminarProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ObtenerProductoUseCase
import pe.edu.upeu.pharmamobil.presentation.cliente.ClienteViewModel
import pe.edu.upeu.pharmamobil.presentation.producto.ProductoViewModel
import pe.edu.upeu.pharmamobil.presentation.sesion.SesionViewModel
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertSame

/**
 * Comprueba que el grafo se ensambla sin arrancar la aplicacion: si una
 * definicion falta o esta declarada con el tipo equivocado, falla aqui.
 *
 * Se cargan los cuatro modulos, incluido presentationModule: construir un
 * ViewModel es justo lo que se rompe al cambiar un constructor, y antes era
 * lo unico que el grafo no cubria. Como los ViewModel arrancan una carga en
 * su init, hace falta un Dispatchers.Main de prueba.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AppModuleTest {

    @BeforeTest
    fun instalarMain() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun detenerKoin() {
        stopKoin()
        Dispatchers.resetMain()
    }

    private fun grafoCompleto(): Koin = startKoin {
        allowOverride(true)
        modules(
            dataModule,
            domainModule,
            module { single<ProductoRepository> { FakeProductoRepository() } },
            presentationModule,
            platformModule,
            // El Compartidor real necesita Context/UIKit; en pruebas se sustituye por un doble.
            module { single<Compartidor> { FakeCompartidor() } },
            module { single<AlmacenSeguro> { AlmacenSeguroFake() }; single<CreadorDeDriver> { object : CreadorDeDriver { override fun crear() = crearDriverEnMemoria().also { PharmaDb.Schema.create(it) } } } }
        )
    }.koin

    @Test
    fun registraLaImplementacionOfflineDeProductos() {
        val koin = startKoin {
            allowOverride(true)
            modules(dataModule, platformModule, module { single<AlmacenSeguro> { AlmacenSeguroFake() }; single<CreadorDeDriver> { object : CreadorDeDriver { override fun crear() = crearDriverEnMemoria().also { PharmaDb.Schema.create(it) } } } })
        }.koin
        assertIs<ProductoRepositorioOffline>(koin.get<ProductoRepository>())
    }

    @Test
    fun resuelveLosRepositoriosPorSuInterfazDeDominio() {

        val koin = grafoCompleto()

        assertIs<FakeProductoRepository>(koin.get<ProductoRepository>())
        assertIs<ClienteRepositorioEnMemoria>(koin.get<ClienteRepository>())
    }

    @Test
    fun losRepositoriosSonUnicosEnTodaLaAplicacion() {

        val koin = grafoCompleto()

        assertSame(
            koin.get<ProductoRepository>(),
            koin.get<ProductoRepository>()
        )
        assertSame(
            koin.get<ClienteRepository>(),
            koin.get<ClienteRepository>()
        )
    }

    @Test
    fun resuelveLosCuatroCasosDeUsoConSusRepositorios() {

        val koin = grafoCompleto()

        koin.get<RegistrarProductoUseCase>()
        koin.get<ListarProductosUseCase>()
        koin.get<ObtenerProductoUseCase>()
        koin.get<ActualizarProductoUseCase>()
        koin.get<EliminarProductoUseCase>()
        koin.get<RegistrarClienteUseCase>()
        koin.get<ListarClientesUseCase>()
    }

    @Test
    fun resuelveLosViewModelConSusCasosDeUso() {

        val koin = grafoCompleto()

        koin.get<ProductoViewModel>()
        koin.get<ClienteViewModel>()
        koin.get<SesionViewModel>()
    }
}

private class AlmacenSeguroFake : AlmacenSeguro {
    private var token: String? = null
    override fun guardarToken(token: String) { this.token = token }
    override fun leerToken(): String? = token
    override fun borrarToken() { token = null }
}
