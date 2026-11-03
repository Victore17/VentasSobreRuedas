package victor.mexico.ventassobreruedas.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale

enum class PeriodType { WEEK, MONTH }

/** Semana (de lunes a domingo) o mes calendario que empieza en [start]; el fin es exclusivo. */
data class Period(val type: PeriodType, val start: LocalDate) {

    val endExclusive: LocalDate
        get() = when (type) {
            PeriodType.WEEK -> start.plusWeeks(1)
            PeriodType.MONTH -> start.plusMonths(1)
        }

    val lastDay: LocalDate get() = endExclusive.minusDays(1)

    /** Todos los días del periodo, en orden. */
    val days: List<LocalDate>
        get() = generateSequence(start) { it.plusDays(1) }.takeWhile { it < endExclusive }.toList()

    operator fun contains(date: LocalDate): Boolean = date >= start && date < endExclusive

    fun previous(): Period = Period(type, shift(-1))

    fun next(): Period = Period(type, shift(1))

    private fun shift(amount: Long): LocalDate = when (type) {
        PeriodType.WEEK -> start.plusWeeks(amount)
        PeriodType.MONTH -> start.plusMonths(amount)
    }

    companion object {
        /** Periodo de tipo [type] que contiene [date]. */
        fun containing(type: PeriodType, date: LocalDate): Period = Period(
            type,
            when (type) {
                PeriodType.WEEK -> date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                PeriodType.MONTH -> date.withDayOfMonth(1)
            },
        )
    }
}

/**
 * Nombre del periodo: "26 oct – 1 nov 2026" para una semana (con el año en ambos extremos si
 * cruza de año) y "Noviembre 2026" para un mes. "LLLL" es el nombre del mes sin declinar,
 * necesario en ruso ("ноябрь", no "ноября").
 */
fun formatPeriodLabel(period: Period, locale: Locale): String = when (period.type) {
    PeriodType.WEEK -> {
        val end = period.lastDay
        val startPattern = if (period.start.year == end.year) "d MMM" else "d MMM yyyy"
        val from = DateTimeFormatter.ofPattern(startPattern, locale).format(period.start)
        val to = DateTimeFormatter.ofPattern("d MMM yyyy", locale).format(end)
        "$from – $to"
    }
    PeriodType.MONTH ->
        DateTimeFormatter.ofPattern("LLLL yyyy", locale).format(period.start).capitalized(locale)
}

/** Día corto para la gráfica y "Mejor día": "Lun", "Mon", "Пн". */
fun formatShortDayOfWeek(date: LocalDate, locale: Locale): String =
    date.dayOfWeek.getDisplayName(TextStyle.SHORT, locale).capitalized(locale)

/** Día de la semana y número: "Lun 2", "Mon 2", "Пн 2". */
fun formatShortDate(date: LocalDate, locale: Locale): String =
    "${formatShortDayOfWeek(date, locale)} ${date.dayOfMonth}"

/** Variación porcentual respecto al periodo anterior; null si antes no hubo ventas. */
fun changePercent(currentCents: Long, previousCents: Long): Int? {
    if (previousCents <= 0) return null
    return Math.round((currentCents - previousCents) * 100.0 / previousCents).toInt()
}

private fun String.capitalized(locale: Locale) = replaceFirstChar { it.titlecase(locale) }
