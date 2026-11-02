package victor.mexico.ventassobreruedas.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.util.Locale

class DatesTest {

    @Test
    fun formatsLongDateInSpanish() {
        assertEquals("Lunes 05 de octubre de 2026", formatLongDate(LocalDate.of(2026, 10, 5)))
        assertEquals("Sábado 03 de enero de 2026", formatLongDate(LocalDate.of(2026, 1, 3)))
    }

    @Test
    fun formatsLongDateWithOtherLanguagePatterns() {
        val date = LocalDate.of(2026, 11, 2)
        assertEquals("Monday, November 2, 2026", formatLongDate(date, "EEEE, MMMM d, yyyy", Locale.ENGLISH))
        // Ruso: día de la semana con mayúscula y el mes en genitivo ("ноября").
        assertEquals(
            "Понедельник, 2 ноября 2026",
            formatLongDate(date, "EEEE, d MMMM yyyy", Locale.forLanguageTag("ru")),
        )
    }
}
