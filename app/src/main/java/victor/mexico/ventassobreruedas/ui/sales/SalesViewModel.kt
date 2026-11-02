package victor.mexico.ventassobreruedas.ui.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import victor.mexico.ventassobreruedas.data.repository.Sale
import victor.mexico.ventassobreruedas.data.repository.SalesRepository
import victor.mexico.ventassobreruedas.util.parsePriceToCents
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.LocalDate

data class SaleForm(
    val productName: String = "",
    val price: String = "",
    val productError: Boolean = false,
    val priceError: Boolean = false,
)

data class SalesUiState(
    val date: LocalDate,
    val isToday: Boolean = true,
    val sales: List<Sale> = emptyList(),
    val totalCents: Long = 0,
)

/** Registro de ventas para [date]; normalmente hoy, o el día abierto en Detalles. */
class SalesViewModel(
    private val repository: SalesRepository,
    date: LocalDate,
) : ViewModel() {

    private val _form = MutableStateFlow(SaleForm())
    val form: StateFlow<SaleForm> = _form.asStateFlow()

    // No se permiten fechas futuras.
    private val date = minOf(date, repository.today())
    private val isToday = this.date == repository.today()

    val uiState: StateFlow<SalesUiState> = combine(
        repository.observeSales(this.date),
        repository.observeTotalCents(this.date),
    ) { sales, total -> SalesUiState(this.date, isToday, sales, total) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SalesUiState(this.date, isToday))

    fun onProductNameChange(value: String) =
        _form.update { it.copy(productName = value, productError = false) }

    fun onPriceChange(value: String) = _form.update { it.copy(price = value, priceError = false) }

    /** Devuelve true si la venta se registró (el formulario era válido). */
    fun addSale(): Boolean {
        val current = _form.value
        val name = current.productName.trim()
        val cents = parsePriceToCents(current.price)
        if (name.isEmpty() || cents == null) {
            _form.update { it.copy(productError = name.isEmpty(), priceError = cents == null) }
            return false
        }
        repository.addSale(name, cents, date)
        _form.value = SaleForm()
        return true
    }

    fun deleteSale(id: String) = repository.deleteSale(id)

    companion object {
        fun factory(repository: SalesRepository, date: LocalDate) = viewModelFactory {
            initializer { SalesViewModel(repository, date) }
        }
    }
}
