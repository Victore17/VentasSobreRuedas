package victor.mexico.ventassobreruedas.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

/** Convierte un texto como "12.50" o "12,5" a centavos. Devuelve null si no es válido o es <= 0. */
fun parsePriceToCents(input: String): Long? {
    val normalized = input.trim().replace(',', '.')
    val value = normalized.toBigDecimalOrNull() ?: return null
    if (value <= BigDecimal.ZERO || value.scale() > 2) return null
    return value.setScale(2, RoundingMode.UNNECESSARY).movePointRight(2).longValueExact()
}

fun formatCents(cents: Long, locale: Locale = Locale.getDefault()): String =
    NumberFormat.getCurrencyInstance(locale).format(BigDecimal.valueOf(cents, 2))
