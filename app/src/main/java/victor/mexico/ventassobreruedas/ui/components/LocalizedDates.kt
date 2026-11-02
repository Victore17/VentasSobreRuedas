package victor.mexico.ventassobreruedas.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import victor.mexico.ventassobreruedas.R
import victor.mexico.ventassobreruedas.util.formatLongDate
import java.time.LocalDate
import java.util.Locale

/**
 * Fecha larga en el idioma de la app ("Lunes 02 de noviembre de 2026", "Monday, November 2, 2026",
 * "Понедельник, 2 ноября 2026"). El patrón y el idioma salen de strings.xml, así la fecha siempre
 * coincide con el idioma del resto de los textos, que Android elige según el idioma del teléfono.
 */
@Composable
fun localizedLongDate(date: LocalDate): String {
    val pattern = stringResource(R.string.date_format)
    val languageTag = stringResource(R.string.date_locale)
    return remember(date, pattern, languageTag) {
        formatLongDate(date, pattern, Locale.forLanguageTag(languageTag))
    }
}
