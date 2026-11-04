package pe.edu.upeu.pharmamobil.data.local

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobil.db.PharmaDb
import pe.edu.upeu.pharmamobil.domain.model.Producto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EstadoEnvioLocalTest {
    private val producto = Producto(0, "Pendiente de prueba", 5.5, 15, categoriaId = 1)

    @Test
    fun contadoresObservablesYFallidaPermanenteExcluidaDeEnvio() = runTest {
        val driver = crearDriverEnMemoria()
        try {
            val local = ProductoLocal(baseEnMemoria(driver))
            val uno = local.guardarPendiente(producto)
            val dos = local.guardarPendiente(producto.copy(nombre = "Otro pendiente"))
            assertEquals(2, local.observarPendientes().first())
            local.sumarIntento(uno.id, "Servidor no disponible")
            assertEquals(1, local.porId(uno.id)?.intentos)
            local.marcarFallida(dos.id, "Conflicto")
            assertEquals(1, local.observarPendientes().first())
            assertEquals(1, local.observarFallidas().first())
            assertEquals(listOf(uno.id), local.pendientesPorEnviar().map { it.id })
            assertEquals("Conflicto", local.porId(dos.id)?.ultimoError)
            assertTrue(local.porId(dos.id)!!.pendiente)
            local.sumarIntento(dos.id, "No debe cambiar")
            assertEquals(Producto.MAX_INTENTOS, local.porId(dos.id)?.intentos)
            assertEquals("Conflicto", local.porId(dos.id)?.ultimoError)
        } finally { driver.close() }
    }

    @Test
    fun bajadaConColisionNoSobrescribePendienteNiFallida() {
        val driver = crearDriverEnMemoria()
        try {
            val local = ProductoLocal(baseEnMemoria(driver))
            val pendiente = local.guardarPendiente(producto)
            val fallida = local.guardarPendiente(producto.copy(nombre = "Fallida"))
            local.marcarFallida(fallida.id, "Validación")
            val antes = local.listar()
            local.reemplazarSincronizados(listOf(
                producto.copy(id = pendiente.id, nombre = "Colisión pendiente"),
                producto.copy(id = fallida.id, nombre = "Colisión fallida"),
                producto.copy(id = 45)))
            assertTrue(local.listar().containsAll(antes))
            assertEquals(3, local.listar().size)
            local.corregirPendiente(local.porId(fallida.id)!!.copy(precio = 7.5))
            assertEquals(3, local.listar().size)
            assertEquals(0, local.porId(fallida.id)?.intentos)
            assertEquals(7.5, local.porId(fallida.id)?.precio)
        } finally { driver.close() }
    }

    @Test
    fun migrarVersionDosConservaTodosLosCamposYMarcaDeSincronizacion() {
        val driver = crearDriverEnMemoria()
        try {
            // Esquema real anterior: ocho columnas, antes de ejecutar 2.sqm.
            driver.execute(null, """CREATE TABLE producto (
                id INTEGER NOT NULL PRIMARY KEY, nombre TEXT NOT NULL, precio REAL NOT NULL,
                stock INTEGER NOT NULL, pendiente INTEGER NOT NULL DEFAULT 0,
                estado INTEGER NOT NULL DEFAULT 1, categoriaId INTEGER NOT NULL DEFAULT 0,
                categoriaNombre TEXT)""", 0)
            driver.execute(null, "CREATE TABLE Sincronizacion(id INTEGER NOT NULL PRIMARY KEY CHECK(id=1), ultimaMillis INTEGER NOT NULL)", 0)
            driver.execute(null, "INSERT INTO producto VALUES(-1,'Guardado sin red',5.5,15,1,1,2,'Categoría anterior')", 0)
            driver.execute(null, "INSERT INTO producto VALUES(103,'Confirmado',7.25,8,0,0,1,'Categoría existente')", 0)
            driver.execute(null, "INSERT INTO Sincronizacion VALUES(1,1793820000000)", 0)
            PharmaDb.Schema.migrate(driver, 2, PharmaDb.Schema.version)
            val local = ProductoLocal(PharmaDb(driver))
            assertEquals(3L, PharmaDb.Schema.version)
            assertEquals(2, local.listar().size)
            assertEquals(producto.copy(id = -1, nombre = "Guardado sin red", pendiente = true,
                categoriaId = 2, categoriaNombre = "Categoría anterior"), local.porId(-1))
            assertEquals(Producto(103, "Confirmado", 7.25, 8, false, 1, "Categoría existente"), local.porId(103))
            assertEquals(1793820000000, local.ultimaSincronizacion())
        } finally { driver.close() }
    }
}
