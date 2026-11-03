package victor.mexico.ventassobreruedas.data.repository

import victor.mexico.ventassobreruedas.util.Period
import java.time.LocalDate
import java.time.ZoneId

/** Totales de un periodo (semana o mes) para el resumen y el PDF. */
data class PeriodSummary(
    val period: Period,
    /** Todos los días del periodo en orden, incluidos los que no tuvieron ventas (para la gráfica). */
    val days: List<SalesDay>,
    /** Ventas del periodo en orden cronológico. */
    val sales: List<Sale>,
) {
    val totalCents: Long get() = days.sumOf { it.totalCents }
    val count: Int get() = sales.size
    val activeDays: List<SalesDay> get() = days.filter { it.count > 0 }

    /** Promedio de los días en que sí se vendió; los días sin ventas no lo bajan. */
    val averagePerActiveDayCents: Long
        get() = activeDays.size.let { if (it == 0) 0 else totalCents / it }

    val bestDay: SalesDay? get() = activeDays.maxByOrNull { it.totalCents }
}

/** Agrupa [sales] por día con [zone], la misma zona que usa el repositorio para los rangos. */
fun buildPeriodSummary(period: Period, sales: List<Sale>, zone: ZoneId): PeriodSummary {
    fun Sale.day(): LocalDate = createdAt.atZone(zone).toLocalDate()
    val inPeriod = sales.filter { it.day() in period }.sortedBy { it.createdAt }
    val byDay = inPeriod.groupBy { it.day() }
    val days = period.days.map { date ->
        val daySales = byDay[date].orEmpty()
        SalesDay(date, daySales.size, daySales.sumOf { it.priceCents })
    }
    return PeriodSummary(period, days, inPeriod)
}
