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

data class SaleForm(
    val productName: String = "",
    val price: String = "",
    val productError: Boolean = false,
    val priceError: Boolean = false,
)

data class SalesUiState(
    val sales: List<Sale> = emptyList(),
    val totalCents: Long = 0,
)

class SalesViewModel(private val repository: SalesRepository) : ViewModel() {

    private val _form = MutableStateFlow(SaleForm())
    val form: StateFlow<SaleForm> = _form.asStateFlow()

    private val today = repository.today()

    val uiState: StateFlow<SalesUiState> = combine(
        repository.observeSales(today),
        repository.observeTotalCents(today),
    ) { sales, total -> SalesUiState(sales, total) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SalesUiState())

    fun onProductNameChange(value: String) =
        _form.update { it.copy(productName = value, productError = false) }

    fun onPriceChange(value: String) = _form.update { it.copy(price = value, priceError = false) }

    fun addSale() {
        val current = _form.value
        val name = current.productName.trim()
        val cents = parsePriceToCents(current.price)
        if (name.isEmpty() || cents == null) {
            _form.update { it.copy(productError = name.isEmpty(), priceError = cents == null) }
            return
        }
        repository.addSale(name, cents)
        _form.value = SaleForm()
    }

    fun deleteSale(id: String) = repository.deleteSale(id)

    companion object {
        fun factory(repository: SalesRepository) = viewModelFactory {
            initializer { SalesViewModel(repository) }
        }
    }
}
