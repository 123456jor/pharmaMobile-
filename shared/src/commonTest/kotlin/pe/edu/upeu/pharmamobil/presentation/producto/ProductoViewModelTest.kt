package pe.edu.upeu.pharmamobil.presentation.producto

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.cancel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pe.edu.upeu.pharmamobil.data.repository.FakeProductoRepository
import pe.edu.upeu.pharmamobil.domain.platform.FakeCompartidor
import pe.edu.upeu.pharmamobil.domain.error.ErrorApi
import pe.edu.upeu.pharmamobil.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.usecase.ActualizarProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.EliminarProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ListarProductosUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ObtenerProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.RegistrarProductoUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ProductoViewModelTest {
    @BeforeTest
    fun instalarMain() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun restaurarMain() = Dispatchers.resetMain()

    private val compartidor = FakeCompartidor()

    private fun nuevoViewModel(repository: FakeProductoRepository = FakeProductoRepository()) =
        ProductoViewModel(
            registrarProducto = RegistrarProductoUseCase(repository),
            repositorio = repository,
            actualizarProducto = ActualizarProductoUseCase(repository),
            eliminarProducto = EliminarProductoUseCase(repository),
            obtenerProducto = ObtenerProductoUseCase(repository),
            compartidor = compartidor
        )

    private fun producto(id: Long = 1L) = Producto(
        id = id, nombre = "Paracetamol", precio = 12.5, stock = 5, categoriaId = 3L
    )

    @Test
    fun consultaLocalMuestraCacheYBloqueaSincronizacionYMutaciones() = runTest {
        val repositorio = FakeProductoRepository(mutableListOf(producto())).apply {
            // Si se intenta escribir, el test falla por la excepción en lugar de ocultarla.
            fallaAlRegistrar = IllegalStateException("No se debe registrar en modo consulta")
        }
        val vm = ProductoViewModel(RegistrarProductoUseCase(repositorio), repositorio,
            ActualizarProductoUseCase(repositorio), EliminarProductoUseCase(repositorio),
            ObtenerProductoUseCase(repositorio), compartidor, soloConsulta = true)
        try {
            advanceUntilIdle()
            vm.onNombreChange("No debe guardarse")
            vm.onPrecioChange("12")
            vm.onStockChange("3")
            vm.onCategoriaIdChange("1")
            vm.registrar()
            vm.eliminar(1)
            vm.sincronizar()
            advanceUntilIdle()
            assertEquals(0, repositorio.cantidadListados, "La consulta local no debe iniciar sincronización")
            assertTrue(repositorio.observar().value.single().estado, "No debe eliminar el producto")
            val fase = assertIs<ProductoUiState.Fase.ConProductos>(vm.uiState.value.fase)
            assertEquals("Paracetamol", fase.productos.single().nombre)
            assertEquals(1, repositorio.listar().size)
            assertNull(vm.uiState.value.mensajeExito)
            assertIs<ProductoUiState.Operacion.Inactiva>(vm.uiState.value.operacion)
        } finally { vm.viewModelScope.cancel() }
    }

    @Test
    fun cargaExitosaMuestraLaListaEsperada() = runTest {
        val viewModel = nuevoViewModel(FakeProductoRepository(mutableListOf(producto())))
        advanceUntilIdle()
        val fase = assertIs<ProductoUiState.Fase.ConProductos>(viewModel.uiState.value.fase)
        assertEquals("Paracetamol", fase.productos.single().nombre)
        // Cada plataforma formatea a su manera (p. ej. espacio no separable en JVM, o sin espacio):
        // se valida el simbolo y el monto, no el separador exacto.
        val precio = fase.productos.single().precio
        assertTrue(precio.startsWith("S/"), "Debe iniciar con S/ pero fue: $precio")
        assertTrue(precio.endsWith("12.50"), "Debe terminar en 12.50 pero fue: $precio")
    }

    @Test
    fun listadoVacioTerminaEnSinProductos() = runTest {
        val viewModel = nuevoViewModel()
        advanceUntilIdle()
        assertIs<ProductoUiState.Fase.SinProductos>(viewModel.uiState.value.fase)
    }

    @Test
    fun validacionDelServidorSeMuestraEnElFormularioSinCambiarLaFase() = runTest {
        val repository = FakeProductoRepository().apply {
            fallaAlRegistrar = ErrorApiException(
                ErrorApi.Validacion(mapOf("nombre" to "Muy corto", "precio" to "Debe ser mayor o igual que 0.01"))
            )
        }
        val viewModel = nuevoViewModel(repository)
        viewModel.onNombreChange("AB")
        viewModel.onPrecioChange("0")
        viewModel.onStockChange("4")
        viewModel.onCategoriaIdChange("3")
        viewModel.registrar()
        advanceUntilIdle()

        val estado = viewModel.uiState.value
        assertEquals("Muy corto", estado.formulario.nombreError)
        assertEquals("Debe ser mayor o igual que 0.01", estado.formulario.precioError)
        assertIs<ProductoUiState.Fase.SinProductos>(estado.fase)
        assertIs<ProductoUiState.Operacion.Inactiva>(estado.operacion)
    }

    @Test
    fun precioCeroSeEnviaParaQueLoValideElServidor() = runTest {
        val repository = FakeProductoRepository()
        val viewModel = nuevoViewModel(repository)
        viewModel.onNombreChange("AB")
        viewModel.onPrecioChange("0")
        viewModel.onStockChange("4")
        viewModel.onCategoriaIdChange("3")
        viewModel.registrar()
        advanceUntilIdle()
        assertEquals(1, repository.listar().size)
        assertEquals(0.0, repository.listar().single().precio)
    }

    @Test
    fun eliminarSeReflejaPorFlowSinRecargarLaListaYConfirmaExito() = runTest {
        val gate = CompletableDeferred<Unit>()
        val started = CompletableDeferred<Unit>()
        val repository = FakeProductoRepository(mutableListOf(producto())).apply {
            antesDeEliminar = { started.complete(Unit); gate.await() }
        }
        val viewModel = nuevoViewModel(repository)
        advanceUntilIdle()
        val listadosAntes = repository.cantidadListados

        viewModel.eliminar(1L)
        runCurrent()
        started.await()
        assertEquals(
            ProductoUiState.Operacion.EnCurso(ProductoUiState.Tipo.Eliminar),
            viewModel.uiState.value.operacion
        )

        gate.complete(Unit)
        advanceUntilIdle()
        assertIs<ProductoUiState.Fase.SinProductos>(viewModel.uiState.value.fase)
        assertIs<ProductoUiState.Operacion.Inactiva>(viewModel.uiState.value.operacion)
        assertEquals("Producto dado de baja", viewModel.uiState.value.mensajeExito)
        assertEquals(listadosAntes, repository.cantidadListados)
    }

    @Test
    fun productosInactivosNoAparecenEnElInventarioActivo() = runTest {
        val activos = producto(id = 1L)
        val inactivos = producto(id = 2L).copy(estado = false)
        val viewModel = nuevoViewModel(FakeProductoRepository(mutableListOf(activos, inactivos)))
        advanceUntilIdle()

        val fase = assertIs<ProductoUiState.Fase.ConProductos>(viewModel.uiState.value.fase)
        assertEquals(listOf(1L), fase.productos.map { it.id })
    }

    @Test
    fun errorDeRedAlEliminarConservaLaListaPrevia() = runTest {
        val repository = FakeProductoRepository(mutableListOf(producto())).apply {
            fallaAlEliminar = ErrorApiException(ErrorApi.SinConexion)
        }
        val viewModel = nuevoViewModel(repository)
        advanceUntilIdle()
        val faseAnterior = viewModel.uiState.value.fase
        viewModel.eliminar(1L)
        advanceUntilIdle()

        assertEquals(faseAnterior, viewModel.uiState.value.fase)
        assertEquals("No hay conexión con PharmaSoft", assertIs<ProductoUiState.Operacion.Fallida>(viewModel.uiState.value.operacion).mensaje)
    }

    @Test
    fun cancelarLaCorrutinaNoProduceEstadoFallidoNiMensajeDeError() = runTest {
        val gate = CompletableDeferred<Unit>()
        val started = CompletableDeferred<Unit>()
        val repository = FakeProductoRepository().apply {
            antesDeRegistrar = { started.complete(Unit); gate.await() }
        }
        val viewModel = nuevoViewModel(repository)
        viewModel.onNombreChange("Paracetamol")
        viewModel.onPrecioChange("12.50")
        viewModel.onStockChange("5")
        viewModel.onCategoriaIdChange("3")
        viewModel.registrar()
        runCurrent()
        started.await()

        viewModel.viewModelScope.cancel()
        runCurrent()

        assertTrue(viewModel.uiState.value.operacion !is ProductoUiState.Operacion.Fallida)
        assertNull(viewModel.uiState.value.mensajeExito)
    }

    @Test
    fun compartirEnviaAlCompartidorElTextoArmadoEnCodigoComun() = runTest {
        val repository = FakeProductoRepository(mutableListOf(producto())).apply {
            fallaAlObtener = ErrorApiException(ErrorApi.SinConexion)
        }
        val viewModel = nuevoViewModel(repository)
        advanceUntilIdle()
        val ui = (viewModel.uiState.value.fase as ProductoUiState.Fase.ConProductos).productos.single()

        viewModel.compartir(ui)
        advanceUntilIdle()

        val texto = compartidor.textos.single()
        assertTrue(texto.startsWith("Paracetamol"), texto)
        assertTrue(texto.contains("12.50"), texto)
        assertTrue(texto.endsWith("Stock: 5"), texto)
        assertEquals(0, repository.cantidadObtener, "Compartir un producto visible no debe necesitar otra petición de red")
    }
}
