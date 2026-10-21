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
    val pendiente: Boolean = false
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
    pendiente = pendiente
)
