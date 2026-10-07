package pe.edu.upeu.pharmamobil.platform

import kotlin.test.Test
import kotlin.test.assertTrue

class FormatoTest {
    @Test
    fun formateaSolesConDosDecimales() {
        val texto = formatearSoles(1234.5)
        assertTrue(texto.startsWith("S/"), texto)
        assertTrue(texto.endsWith("234.50"), texto)
    }
}
