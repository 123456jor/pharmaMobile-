package pe.edu.upeu.pharmamobil.presentation.producto

import kotlin.math.roundToLong
import pe.edu.upeu.pharmamobil.domain.model.Producto


data class ProductoUi(
    val id: Long,
    val nombre: String,
    val precio: String,
    val stock: String,
    val requiereReposicion: Boolean,
    val precioValor: Double,
    val stockValor: Int,
    val categoriaId: Long
)

fun Producto.aUi(): ProductoUi = ProductoUi(
    id = id,
    nombre = nombre,
    precio = precio.enSoles(),
    stock = "$stock u.",
    requiereReposicion = requiereReposicion,
    precioValor = precio,
    stockValor = stock,
    categoriaId = categoriaId
)

/** Kotlin comun no trae String.format, asi que armamos los dos decimales a mano. */
private fun Double.enSoles(): String {

    val centavos = (this * 100).roundToLong()

    val enteros = centavos / 100
    val decimales = (centavos % 100).toString().padStart(2, '0')

    return "S/ $enteros.$decimales"
}
