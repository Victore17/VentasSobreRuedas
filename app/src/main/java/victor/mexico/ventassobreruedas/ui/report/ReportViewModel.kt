package victor.mexico.ventassobreruedas.ui.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import victor.mexico.ventassobreruedas.data.repository.PeriodSummary
import victor.mexico.ventassobreruedas.data.repository.SalesRepository
import victor.mexico.ventassobreruedas.data.repository.buildPeriodSummary
import victor.mexico.ventassobreruedas.util.Period
import victor.mexico.ventassobreruedas.util.PeriodType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

data class ReportUiState(
    val period: Period,
    val today: LocalDate,
    /** null mientras llegan los datos del periodo. */
    val summary: PeriodSummary? = null,
    val previousTotalCents: Long = 0,
) {
    /** No se avanza a periodos que todavía no empiezan. */
    val canGoNext: Boolean get() = period.endExclusive <= today
}

@OptIn(ExperimentalCoroutinesApi::class)
class ReportViewModel(private val repository: SalesRepository) : ViewModel() {

    private val period = MutableStateFlow(Period.containing(PeriodType.WEEK, repository.today()))

    val uiState: StateFlow<ReportUiState> = period
        .flatMapLatest { current ->
            val previous = current.previous()
            combine(
                repository.observeSalesBetween(current.start, current.endExclusive),
                repository.observeSalesBetween(previous.start, previous.endExclusive),
            ) { sales, previousSales ->
                ReportUiState(
                    period = current,
                    today = repository.today(),
                    summary = buildPeriodSummary(current, sales, repository.zone),
                    previousTotalCents = previousSales.sumOf { it.priceCents },
                )
            }
                // El periodo nuevo se publica de inmediato, sin esperar a Firestore.
                .onStart { emit(ReportUiState(current, repository.today())) }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            ReportUiState(period.value, repository.today()),
        )

    /** Cambia entre semana y mes; si se veía el periodo actual, se queda en el actual. */
    fun selectType(type: PeriodType) {
        val current = period.value
        if (current.type == type) return
        val today = repository.today()
        period.value = Period.containing(type, if (today in current) today else current.start)
    }

    fun previousPeriod() {
        period.value = period.value.previous()
    }

    fun nextPeriod() {
        if (period.value.endExclusive <= repository.today()) period.value = period.value.next()
    }

    companion object {
        fun factory(repository: SalesRepository) = viewModelFactory {
            initializer { ReportViewModel(repository) }
        }
    }
}
