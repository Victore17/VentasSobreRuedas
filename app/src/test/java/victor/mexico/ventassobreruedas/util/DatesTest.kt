package victor.mexico.ventassobreruedas.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class DatesTest {

    @Test
    fun formatsLongDateInSpanish() {
        assertEquals("Lunes 05 de octubre de 2026", formatLongDate(LocalDate.of(2026, 10, 5)))
        assertEquals("Sábado 03 de enero de 2026", formatLongDate(LocalDate.of(2026, 1, 3)))
    }
}
