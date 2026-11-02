package victor.mexico.ventassobreruedas.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val spanish: Locale = Locale.forLanguageTag("es")

private val longDateFormatter = DateTimeFormatter.ofPattern("EEEE dd 'de' MMMM 'de' yyyy", spanish)

/** Formatea la fecha como "Lunes 05 de octubre de 2026", sin importar el idioma del teléfono. */
fun formatLongDate(date: LocalDate): String =
    longDateFormatter.format(date).replaceFirstChar { it.titlecase(spanish) }
