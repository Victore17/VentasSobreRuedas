package victor.mexico.ventassobreruedas.ui.report

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import victor.mexico.ventassobreruedas.data.repository.SalesDay
import victor.mexico.ventassobreruedas.util.formatCentsShort
import victor.mexico.ventassobreruedas.util.formatShortDayOfWeek
import java.time.LocalDate
import java.util.Locale

private val ChartHeight = 160.dp

/**
 * Una barra por día. En la semana cada barra lleva su monto y el nombre del día; en el mes
 * (hasta 31 barras) solo se numeran algunos días para que las etiquetas no se encimen.
 * Tocar una barra con ventas abre ese día.
 */
@Composable
fun SalesBarChart(
    days: List<SalesDay>,
    showValues: Boolean,
    locale: Locale,
    onDayClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val max = days.maxOfOrNull { it.totalCents }?.takeIf { it > 0 } ?: 1
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(if (showValues) 6.dp else 2.dp),
    ) {
        days.forEach { day ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(enabled = day.count > 0) { onDayClick(day.date) },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (showValues) {
                    Text(
                        if (day.count > 0) formatCentsShort(day.totalCents) else "",
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ChartHeight),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    val fraction = day.totalCents.toFloat() / max
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            // Los días sin ventas se ven como una línea en la base.
                            .heightIn(min = 2.dp)
                            .fillMaxHeight(fraction)
                            .background(
                                if (day.count > 0) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant
                                },
                                RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp),
                            ),
                    )
                }
                Text(
                    dayLabel(day.date, showValues, locale),
                    // En el mes la columna es más angosta que "10": la etiqueta puede salirse de ella.
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .wrapContentWidth(unbounded = true),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    softWrap = false,
                )
            }
        }
    }
}

/** En la semana "Lun"; en el mes solo los días 1, 5, 10, 15… */
internal fun dayLabel(date: LocalDate, weekly: Boolean, locale: Locale): String = when {
    weekly -> formatShortDayOfWeek(date, locale)
    date.dayOfMonth == 1 || date.dayOfMonth % 5 == 0 -> date.dayOfMonth.toString()
    else -> ""
}

/** Ícono de gráfica de barras (no viene en material-icons-core). */
val Icons.Filled.BarChart: ImageVector get() = barChartIcon

private val barChartIcon: ImageVector by lazy {
    materialIcon(name = "Filled.BarChart") {
        materialPath {
            moveTo(5f, 9.2f)
            horizontalLineToRelative(3f)
            verticalLineTo(19f)
            horizontalLineTo(5f)
            close()
            moveTo(10.6f, 5f)
            horizontalLineToRelative(2.8f)
            verticalLineToRelative(14f)
            horizontalLineToRelative(-2.8f)
            close()
            moveTo(16.2f, 13f)
            horizontalLineTo(19f)
            verticalLineToRelative(6f)
            horizontalLineToRelative(-2.8f)
            close()
        }
    }
}
