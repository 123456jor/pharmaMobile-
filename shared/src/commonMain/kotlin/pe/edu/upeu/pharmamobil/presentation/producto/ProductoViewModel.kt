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
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.usecase.ActualizarProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.EliminarProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ListarProductosUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ObtenerProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ProductoInvalidoException
import pe.edu.upeu.pharmamobil.domain.usecase.RegistrarProductoUseCase

class ProductoViewModel(
    private val registrarProducto: RegistrarProductoUseCase,
    private val listarProductos: ListarProductosUseCase,
    private val actualizarProducto: ActualizarProductoUseCase,
    private val eliminarProducto: EliminarProductoUseCase,
    private val obtenerProducto: ObtenerProductoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductoUiState())
    val uiState: StateFlow<ProductoUiState> = _uiState.asStateFlow()

    init { cargarProductos() }

    fun cargarProductos() {
        viewModelScope.launch {
            _uiState.update { it.copy(fase = ProductoUiState.Fase.Cargando) }
            listarProductos().fold(
                onSuccess = { productos -> _uiState.update { it.copy(fase = productos.aFase()) } },
                onFailure = { fallo -> _uiState.update {
                    it.copy(fase = ProductoUiState.Fase.Error(mensajeDe((fallo as? ErrorApiException)?.error, fallo)))
                } }
            )
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
            resolverMutacion(resultado, "Producto registrado")
        }
    }

    fun comenzarEdicion(producto: ProductoUi) {
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
                    recargarTrasMutacion("Producto dado de baja", limpiarFormulario = false)
                },
                onFailure = ::manejarFallo
            )
        }
    }

    private fun iniciarOperacion(tipo: ProductoUiState.Tipo): Boolean {
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

    private suspend fun <T> resolverMutacion(resultado: Result<T>, exito: String) {
        resultado.fold(
            onSuccess = { recargarTrasMutacion(exito, limpiarFormulario = true) },
            onFailure = ::manejarFallo
        )
    }

    private suspend fun recargarTrasMutacion(exito: String, limpiarFormulario: Boolean) {
        listarProductos().fold(
            onSuccess = { productos ->
                _uiState.update {
                    it.copy(
                        fase = productos.aFase(),
                        operacion = ProductoUiState.Operacion.Inactiva,
                        mensajeExito = exito,
                        productoEditandoId = if (limpiarFormulario) null else it.productoEditandoId,
                        formulario = if (limpiarFormulario) FormularioProducto() else it.formulario
                    )
                }
            },
            onFailure = { fallo ->
                _uiState.update {
                    it.copy(operacion = ProductoUiState.Operacion.Fallida(
                        "Se completó la operación, pero no se pudo actualizar la lista: ${mensajeDe((fallo as? ErrorApiException)?.error, fallo)}"
                    ))
                }
            }
        )
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
        null -> fallo?.message ?: "Ocurrió un error inesperado"
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
