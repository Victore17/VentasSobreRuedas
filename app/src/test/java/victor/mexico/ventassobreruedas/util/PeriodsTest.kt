package victor.mexico.ventassobreruedas.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import victor.mexico.ventassobreruedas.data.repository.Sale
import victor.mexico.ventassobreruedas.data.repository.SalesDay
import victor.mexico.ventassobreruedas.data.repository.buildPeriodSummary
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

class PeriodsTest {

    private val zone = ZoneId.of("America/Mexico_City")

    @Test
    fun weekGoesFromMondayToSunday() {
        // 2 de noviembre de 2026 es lunes; el domingo 8 sigue en la misma semana.
        val week = Period.containing(PeriodType.WEEK, LocalDate.of(2026, 11, 8))
        assertEquals(LocalDate.of(2026, 11, 2), week.start)
        assertEquals(LocalDate.of(2026, 11, 8), week.lastDay)
        assertEquals(7, week.days.size)
        assertEquals(LocalDate.of(2026, 10, 26), week.previous().start)
    }

    @Test
    fun monthCoversWholeCalendarMonth() {
        val february = Period.containing(PeriodType.MONTH, LocalDate.of(2028, 2, 15))
        assertEquals(LocalDate.of(2028, 2, 1), february.start)
        assertEquals(29, february.days.size) // 2028 es bisiesto
        assertEquals(LocalDate.of(2028, 3, 1), february.endExclusive)
        assertEquals(LocalDate.of(2028, 1, 1), february.previous().start)
        assertTrue(LocalDate.of(2028, 2, 29) in february)
        assertFalse(LocalDate.of(2028, 3, 1) in february)
    }

    @Test
    fun formatsPeriodLabels() {
        val week = Period.containing(PeriodType.WEEK, LocalDate.of(2026, 10, 28))
        assertEquals("26 Oct – 1 Nov 2026", formatPeriodLabel(week, Locale.ENGLISH))
        // Si la semana cruza de año, ambos extremos llevan año.
        val newYear = Period.containing(PeriodType.WEEK, LocalDate.of(2026, 1, 1))
        assertEquals("29 Dec 2025 – 4 Jan 2026", formatPeriodLabel(newYear, Locale.ENGLISH))

        val month = Period.containing(PeriodType.MONTH, LocalDate.of(2026, 11, 2))
        assertEquals("Noviembre 2026", formatPeriodLabel(month, Locale.forLanguageTag("es")))
        // Ruso: nombre del mes sin declinar ("ноябрь", no "ноября").
        assertEquals("Ноябрь 2026", formatPeriodLabel(month, Locale.forLanguageTag("ru")))
    }

    @Test
    fun summaryIncludesEmptyDaysAndTotals() {
        val week = Period.containing(PeriodType.WEEK, LocalDate.of(2026, 11, 2))
        val sales = listOf(
            sale("1", 5000, LocalDate.of(2026, 11, 2), 9),
            sale("2", 3000, LocalDate.of(2026, 11, 2), 18),
            sale("3", 12000, LocalDate.of(2026, 11, 4), 13),
            // Fuera del periodo: no cuenta.
            sale("4", 99900, LocalDate.of(2026, 11, 9), 10),
        )
        val summary = buildPeriodSummary(week, sales, zone)

        assertEquals(7, summary.days.size)
        assertEquals(SalesDay(LocalDate.of(2026, 11, 2), 2, 8000), summary.days[0])
        assertEquals(SalesDay(LocalDate.of(2026, 11, 3), 0, 0), summary.days[1])
        assertEquals(20000, summary.totalCents)
        assertEquals(3, summary.count)
        // Promedio entre los 2 días con ventas, no entre los 7.
        assertEquals(10000, summary.averagePerActiveDayCents)
        assertEquals(LocalDate.of(2026, 11, 4), summary.bestDay?.date)
        assertEquals(listOf("1", "2", "3"), summary.sales.map { it.id })
    }

    @Test
    fun emptySummaryHasNoBestDay() {
        val week = Period.containing(PeriodType.WEEK, LocalDate.of(2026, 11, 2))
        val summary = buildPeriodSummary(week, emptyList(), zone)
        assertEquals(0, summary.totalCents)
        assertEquals(0, summary.averagePerActiveDayCents)
        assertNull(summary.bestDay)
    }

    @Test
    fun computesChangePercent() {
        assertEquals(12, changePercent(11200, 10000))
        assertEquals(-50, changePercent(5000, 10000))
        assertNull(changePercent(5000, 0))
    }

    private fun sale(id: String, cents: Long, day: LocalDate, hour: Int) =
        Sale(id, "Café", cents, day.atTime(hour, 0).atZone(zone).toInstant())
}
