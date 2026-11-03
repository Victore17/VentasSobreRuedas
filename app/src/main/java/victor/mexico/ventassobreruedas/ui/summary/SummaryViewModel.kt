package victor.mexico.ventassobreruedas.ui.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import victor.mexico.ventassobreruedas.data.repository.Sale
import victor.mexico.ventassobreruedas.data.repository.SalesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

data class SummaryUiState(
    val date: LocalDate,
    val today: LocalDate = date,
    val totalCents: Long = 0,
    val sales: List<Sale> = emptyList(),
) {
    val count: Int get() = sales.size
    val isToday: Boolean get() = date == today
}

@OptIn(ExperimentalCoroutinesApi::class)
class SummaryViewModel(
    private val repository: SalesRepository,
    initialDate: LocalDate,
) : ViewModel() {

    private val selectedDate = MutableStateFlow(minOf(initialDate, repository.today()))

    val uiState: StateFlow<SummaryUiState> = selectedDate
        .flatMapLatest { date ->
            combine(
                repository.observeSales(date),
                repository.observeTotalCents(date),
            ) { sales, total -> SummaryUiState(date, repository.today(), total, sales) }
                // La fecha nueva se publica de inmediato, sin esperar a Firestore; si no, el "+"
                // seguiría registrando en el día anterior hasta que llegaran los datos.
                .onStart { emit(SummaryUiState(date, repository.today())) }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            SummaryUiState(selectedDate.value),
        )

    /** Cambia el día consultado; no se permiten fechas futuras. */
    fun selectDate(date: LocalDate) {
        selectedDate.value = minOf(date, repository.today())
    }

    fun updateSale(id: String, productName: String, priceCents: Long) =
        repository.updateSale(id, productName, priceCents)

    fun deleteSale(id: String) = repository.deleteSale(id)

    companion object {
        fun factory(repository: SalesRepository, date: LocalDate) = viewModelFactory {
            initializer { SummaryViewModel(repository, date) }
        }
    }
}
