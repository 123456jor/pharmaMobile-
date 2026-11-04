package pe.edu.upeu.pharmamobil.presentation.producto

import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.platform.formatearSoles


data class ProductoUi(
    val id: Long,
    val nombre: String,
    val precio: String,
    val stock: String,
    val requiereReposicion: Boolean,
    val precioValor: Double,
    val stockValor: Int,
    val categoriaId: Long,
    val pendiente: Boolean = false,
    val intentos: Int = 0,
    val ultimoError: String? = null
)

fun Producto.aUi(): ProductoUi = ProductoUi(
    id = id,
    nombre = nombre,
    precio = formatearSoles(precio),
    stock = "$stock u.",
    requiereReposicion = requiereReposicion,
    precioValor = precio,
    stockValor = stock,
    categoriaId = categoriaId,
    pendiente = pendiente,
    intentos = intentos,
    ultimoError = ultimoError
)

enum class EstadoEnvioUi { Sincronizado, Pendiente, Fallido }
val ProductoUi.estadoEnvio: EstadoEnvioUi get() = when {
    !pendiente -> EstadoEnvioUi.Sincronizado
    intentos >= Producto.MAX_INTENTOS -> EstadoEnvioUi.Fallido
    else -> EstadoEnvioUi.Pendiente
}
val ProductoUi.requiereConciliacion: Boolean get() = ultimoError == pe.edu.upeu.pharmamobil.domain.model.PoliticaEnvio.INCIERTO
val ProductoUi.puedeReintentar: Boolean get() = ultimoError in pe.edu.upeu.pharmamobil.domain.model.PoliticaEnvio.recuperables
