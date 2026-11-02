package victor.mexico.ventassobreruedas.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Locale

class MoneyTest {

    @Test
    fun parsesDotAndCommaDecimals() {
        assertEquals(1250L, parsePriceToCents("12.50"))
        assertEquals(1250L, parsePriceToCents("12,5"))
        assertEquals(700L, parsePriceToCents(" 7 "))
    }

    @Test
    fun rejectsInvalidPrices() {
        assertNull(parsePriceToCents(""))
        assertNull(parsePriceToCents("abc"))
        assertNull(parsePriceToCents("0"))
        assertNull(parsePriceToCents("-3"))
        assertNull(parsePriceToCents("1.234"))
    }

    @Test
    fun formatsCents() {
        assertEquals("$12.50", formatCents(1250, Locale.US))
    }

    @Test
    fun alwaysUsesPesoSymbolWhateverTheLanguage() {
        // Los espacios de agrupación/separación de cada idioma son espacios no separables.
        fun normalized(s: String) = s.replace('\u00A0', ' ').replace('\u202F', ' ')
        assertEquals("$1,540.00", formatCents(154000, Locale.UK))
        assertEquals("1 540,00 $", normalized(formatCents(154000, Locale.forLanguageTag("ru-RU"))))
        assertEquals("$1,540.00", formatCents(154000, Locale.forLanguageTag("es-MX")))
    }
}
