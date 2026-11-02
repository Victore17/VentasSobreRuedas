package victor.mexico.ventassobreruedas.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val spanish: Locale = Locale.forLanguageTag("es")

private const val SPANISH_LONG_DATE = "EEEE dd 'de' MMMM 'de' yyyy"

/**
 * Formatea la fecha con [pattern] en [locale], con la primera letra en mayúscula.
 * Por omisión: "Lunes 05 de octubre de 2026". En la UI se usa `localizedLongDate`,
 * que toma el patrón y el idioma de los recursos según el idioma del teléfono.
 */
fun formatLongDate(
    date: LocalDate,
    pattern: String = SPANISH_LONG_DATE,
    locale: Locale = spanish,
): String = DateTimeFormatter.ofPattern(pattern, locale).format(date).replaceFirstChar { it.titlecase(locale) }
