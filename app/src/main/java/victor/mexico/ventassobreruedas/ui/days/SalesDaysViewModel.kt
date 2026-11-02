package victor.mexico.ventassobreruedas.ui.days

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import victor.mexico.ventassobreruedas.data.repository.SalesDay
import victor.mexico.ventassobreruedas.data.repository.SalesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class SalesDaysUiState(
    val days: List<SalesDay> = emptyList(),
    val isLoading: Boolean = true,
)

class SalesDaysViewModel(repository: SalesRepository) : ViewModel() {

    val uiState: StateFlow<SalesDaysUiState> = repository.observeSalesDays()
        .map { SalesDaysUiState(days = it, isLoading = false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SalesDaysUiState())

    companion object {
        fun factory(repository: SalesRepository) = viewModelFactory {
            initializer { SalesDaysViewModel(repository) }
        }
    }
}
