package pe.edu.upeu.pharmamobil.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class AntiguedadTest {
    @Test fun nuncaSincronizado() { assertEquals("Aún no se ha sincronizado", formatearAntiguedad(999, null)) }
    @Test fun segundosYRelojAtrasado() {
        assertEquals("Actualizado hace unos segundos", formatearAntiguedad(59_999, 0))
        assertEquals("Actualizado hace unos segundos", formatearAntiguedad(0, 1000))
    }
    @Test fun minutos() {
        assertEquals("Actualizado hace 1 minuto", formatearAntiguedad(60_000, 0))
        assertEquals("Actualizado hace 59 minutos", formatearAntiguedad(3_599_999, 0))
    }
    @Test fun horas() {
        assertEquals("Actualizado hace 1 hora", formatearAntiguedad(3_600_000, 0))
        assertEquals("Actualizado hace 23 horas", formatearAntiguedad(86_399_999, 0))
    }
    @Test fun dias() {
        assertEquals("Actualizado hace 1 día", formatearAntiguedad(86_400_000, 0))
        assertEquals("Actualizado hace 2 días", formatearAntiguedad(172_800_000, 0))
    }
}
