package victor.mexico.ventassobreruedas.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

/** Convierte un texto como "12.50" o "12,5" a centavos. Devuelve null si no es válido o es <= 0. */
fun parsePriceToCents(input: String): Long? {
    val normalized = input.trim().replace(',', '.')
    val value = normalized.toBigDecimalOrNull() ?: return null
    if (value <= BigDecimal.ZERO || value.scale() > 2) return null
    return value.setScale(2, RoundingMode.UNNECESSARY).movePointRight(2).longValueExact()
}

/**
 * Formatea centavos como pesos. El formato del número (separadores, posición del símbolo)
 * sigue el idioma del teléfono, pero la moneda siempre es pesos con "$": sin esto, un teléfono
 * en ruso mostraría rublos (₽) y uno de Reino Unido libras (£) para las mismas cantidades.
 */
fun formatCents(cents: Long, locale: Locale = Locale.getDefault()): String =
    pesoFormat(locale).format(BigDecimal.valueOf(cents, 2))

/** Como [formatCents] pero redondeado a pesos ("$1,235"), para etiquetas angostas como las de la gráfica. */
fun formatCentsShort(cents: Long, locale: Locale = Locale.getDefault()): String {
    val format = pesoFormat(locale)
    format.minimumFractionDigits = 0
    format.maximumFractionDigits = 0
    format.roundingMode = RoundingMode.HALF_UP
    return format.format(BigDecimal.valueOf(cents, 2))
}

private fun pesoFormat(locale: Locale): NumberFormat {
    val format = NumberFormat.getCurrencyInstance(locale)
    format.currency = Currency.getInstance("MXN")
    if (format is DecimalFormat) {
        format.decimalFormatSymbols = format.decimalFormatSymbols.apply { currencySymbol = "$" }
    }
    return format
}
