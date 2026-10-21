package pe.edu.upeu.pharmamobil.domain.model

data class Producto(
    val id:Long,
    val nombre:String,
    val precio:Double,
    val stock: Int,
    val estado: Boolean = true,
    val categoriaId: Long = 0L,
    val categoriaNombre: String? = null,
    val pendiente: Boolean = false
) {


    init {
        require(nombre.isNotBlank()) {
            "El nombre del producto no puede estar vacio"
        }
        require(precio.isFinite()) {
            "El precio debe ser un numero finito"
        }
        require(stock >= 0) {
            "El stock no puede ser negativo"
        }
    }

    /**
     * Un producto necesita reposicion cuando su stock cae por debajo del
     * minimo que la botica mantiene en gondola.
     */
    val requiereReposicion: Boolean
        get() = stock < STOCK_MINIMO

    companion object {
        const val STOCK_MINIMO = 10
    }
}
