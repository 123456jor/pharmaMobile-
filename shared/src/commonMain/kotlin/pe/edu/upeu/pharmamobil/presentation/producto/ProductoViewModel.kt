package pe.edu.upeu.pharmamobil.presentation.producto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobil.domain.error.ErrorApi
import pe.edu.upeu.pharmamobil.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobil.domain.model.ErrorSesion
import pe.edu.upeu.pharmamobil.domain.model.ErrorSesionException
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.platform.Compartidor
import pe.edu.upeu.pharmamobil.domain.usecase.ActualizarProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.EliminarProductoUseCase
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository
import kotlinx.coroutines.CancellationException
import pe.edu.upeu.pharmamobil.domain.usecase.ObtenerProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ProductoInvalidoException
import pe.edu.upeu.pharmamobil.domain.usecase.RegistrarProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.comoTextoParaCompartir

class ProductoViewModel(
    private val registrarProducto: RegistrarProductoUseCase,
    private val repositorio: ProductoRepository,
    private val actualizarProducto: ActualizarProductoUseCase,
    private val eliminarProducto: EliminarProductoUseCase,
    private val obtenerProducto: ObtenerProductoUseCase,
    private val compartidor: Compartidor,
    private val soloConsulta: Boolean = false
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductoUiState())
    val uiState: StateFlow<ProductoUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repositorio.observar().collect { productos ->
                _uiState.update { it.copy(fase = productos.aFase()) }
            }
        }
        viewModelScope.launch {
            repositorio.observarUltimaSincronizacion().collect { ultima ->
                _uiState.update { it.copy(ultimaSincronizacion = ultima) }
            }
        }
        if (!soloConsulta) sincronizar()
    }

    fun sincronizar() {
        if (soloConsulta) return
        if (_uiState.value.sincronizando) return
        viewModelScope.launch {
            _uiState.update { it.copy(sincronizando = true) }
            try {
                repositorio.sincronizar()
                _uiState.update { it.copy(avisoRed = null) }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (fallo: Exception) {
                val error = (fallo as? ErrorApiException)?.error
                val aviso = if (error == ErrorApi.SinConexion || error == ErrorApi.TiempoAgotado)
                    "Sin conexión: mostrando datos guardados"
                else mensajeDe(error, fallo)
                _uiState.update { it.copy(avisoRed = aviso) }
            } finally {
                _uiState.update { it.copy(sincronizando = false) }
            }
        }
    }

    fun onNombreChange(valor: String) = actualizarFormulario { copy(nombre = valor, nombreError = null) }
    fun onPrecioChange(valor: String) = actualizarFormulario { copy(precio = valor, precioError = null) }
    fun onStockChange(valor: String) = actualizarFormulario { copy(stock = valor, stockError = null) }
    fun onCategoriaIdChange(valor: String) = actualizarFormulario { copy(categoriaId = valor, categoriaIdError = null) }

    fun registrar() {
        if (!iniciarOperacion(ProductoUiState.Tipo.Crear)) return
        viewModelScope.launch {
            val formulario = _uiState.value.formulario
            val resultado = registrarProducto(
                nombre = formulario.nombre,
                precio = formulario.precio,
                stock = formulario.stock,
                categoriaId = formulario.categoriaId
            )
            resolverMutacion(resultado, "Producto guardado")
        }
    }

    fun comenzarEdicion(producto: ProductoUi) {
        if (!iniciarOperacion(ProductoUiState.Tipo.Actualizar)) return
        viewModelScope.launch {
            obtenerProducto(producto.id).fold(
                onSuccess = { encontrado ->
                    _uiState.update {
                        it.copy(
                            productoEditandoId = encontrado.id,
                            formulario = encontrado.aFormulario(),
                            mensajeExito = null,
                            operacion = ProductoUiState.Operacion.Inactiva
                        )
                    }
                },
                onFailure = { manejarFallo(it) }
            )
        }
    }

    /** El texto se arma en codigo comun; la hoja de compartir la abre cada plataforma. */
    fun compartir(producto: ProductoUi) {
        val productoActual = Producto(
            id = producto.id,
            nombre = producto.nombre,
            precio = producto.precioValor,
            stock = producto.stockValor,
            estado = true,
            categoriaId = producto.categoriaId
        )
        compartidor.compartir(productoActual.comoTextoParaCompartir())
    }

    fun cancelarEdicion() {
        _uiState.update { it.copy(productoEditandoId = null, formulario = FormularioProducto(), mensajeExito = null) }
    }

    fun actualizar() {
        val id = _uiState.value.productoEditandoId ?: return
        if (!iniciarOperacion(ProductoUiState.Tipo.Actualizar)) return
        viewModelScope.launch {
            val formulario = _uiState.value.formulario
            val resultado = actualizarProducto(
                id = id,
                nombre = formulario.nombre,
                precio = formulario.precio,
                stock = formulario.stock,
                categoriaId = formulario.categoriaId
            )
            resolverMutacion(resultado, "Producto actualizado")
        }
    }

    fun eliminar(id: Long) {
        if (!iniciarOperacion(ProductoUiState.Tipo.Eliminar)) return
        viewModelScope.launch {
            eliminarProducto(id).fold(
                onSuccess = {
                    confirmarMutacion("Producto dado de baja", limpiarFormulario = false)
                },
                onFailure = ::manejarFallo
            )
        }
    }

    private fun iniciarOperacion(tipo: ProductoUiState.Tipo): Boolean {
        if (soloConsulta) return false
        if (_uiState.value.operacion is ProductoUiState.Operacion.EnCurso) return false
        _uiState.update {
            it.copy(
                formulario = it.formulario.sinErrores(),
                operacion = ProductoUiState.Operacion.EnCurso(tipo),
                mensajeExito = null
            )
        }
        return true
    }

    private fun <T> resolverMutacion(resultado: Result<T>, exito: String) {
        resultado.fold(
            onSuccess = { confirmarMutacion(exito, limpiarFormulario = true) },
            onFailure = ::manejarFallo
        )
    }

    private fun confirmarMutacion(exito: String, limpiarFormulario: Boolean) {
        _uiState.update {
            it.copy(
                operacion = ProductoUiState.Operacion.Inactiva,
                mensajeExito = exito,
                productoEditandoId = if (limpiarFormulario) null else it.productoEditandoId,
                formulario = if (limpiarFormulario) FormularioProducto() else it.formulario
            )
        }
    }

    private fun manejarFallo(fallo: Throwable) {
        val error = (fallo as? ErrorApiException)?.error
        when {
            error is ErrorApi.Validacion -> mostrarErroresDeCampos(error.porCampo)
            fallo is ProductoInvalidoException -> mostrarErroresDeCampos(
                mapOf(
                    "nombre" to fallo.errores.nombre,
                    "precio" to fallo.errores.precio,
                    "stock" to fallo.errores.stock,
                    "categoriaId" to fallo.errores.categoriaId
                ).filterValues { it != null }.mapValues { it.value!! }
            )
            else -> _uiState.update { it.copy(operacion = ProductoUiState.Operacion.Fallida(mensajeDe(error, fallo))) }
        }
    }

    private fun mostrarErroresDeCampos(errores: Map<String, String>) {
        _uiState.update {
            it.copy(
                operacion = ProductoUiState.Operacion.Inactiva,
                formulario = it.formulario.copy(
                    nombreError = errores["nombre"],
                    precioError = errores["precio"],
                    stockError = errores["stock"],
                    categoriaIdError = errores["categoriaId"]
                )
            )
        }
    }

    private fun mensajeDe(error: ErrorApi?, fallo: Throwable? = null): String = when (error) {
        is ErrorApi.Validacion -> "Revisa los datos ingresados"
        ErrorApi.NoEncontrado -> "No se encontró el producto solicitado"
        is ErrorApi.Conflicto -> error.mensaje
        ErrorApi.Servidor -> "El servidor no pudo completar la operación"
        ErrorApi.SinConexion -> "No hay conexión con PharmaSoft"
        ErrorApi.TiempoAgotado -> "La solicitud tardó demasiado. Inténtalo nuevamente"
        null -> when ((fallo as? ErrorSesionException)?.error) {
            ErrorSesion.SesionExpirada -> "Tu sesión expiró. Inicia sesión nuevamente."
            ErrorSesion.CredencialesInvalidas -> "La sesión ya no es válida. Inicia sesión nuevamente."
            ErrorSesion.SinPermiso -> "No tienes permisos para realizar esta operación."
            ErrorSesion.SinConexion -> "No hay conexión con PharmaSoft."
            null -> fallo?.message ?: "Ocurrió un error inesperado"
        }
    }

    private fun actualizarFormulario(transformar: FormularioProducto.() -> FormularioProducto) {
        _uiState.update { it.copy(formulario = it.formulario.transformar(), mensajeExito = null) }
    }

    private fun FormularioProducto.sinErrores() = copy(
        nombreError = null,
        precioError = null,
        stockError = null,
        categoriaIdError = null
    )

    private fun Producto.aFormulario() = FormularioProducto(
        nombre = nombre,
        precio = precio.toString(),
        stock = stock.toString(),
        categoriaId = categoriaId.takeIf { it > 0 }?.toString().orEmpty(),
        nombreError = null,
        precioError = null,
        stockError = null,
        categoriaIdError = null
    )

    private fun List<Producto>.aFase(): ProductoUiState.Fase {
        val activos = filter(Producto::estado)
        return if (activos.isEmpty()) ProductoUiState.Fase.SinProductos
        else ProductoUiState.Fase.ConProductos(activos.map(Producto::aUi))
    }
}
