package victor.mexico.ventassobreruedas.ui.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import victor.mexico.ventassobreruedas.R
import victor.mexico.ventassobreruedas.ui.components.CenteredContentPadding
import victor.mexico.ventassobreruedas.data.repository.Sale
import victor.mexico.ventassobreruedas.salesRepository
import victor.mexico.ventassobreruedas.ui.theme.MyApplicationTheme
import victor.mexico.ventassobreruedas.util.formatCents
import victor.mexico.ventassobreruedas.ui.components.localizedLongDate
import java.time.Instant
import java.time.LocalDate

@Composable
fun SummaryRoute(
    date: LocalDate,
    onBack: () -> Unit,
    onAddSales: (LocalDate) -> Unit,
    viewModel: SummaryViewModel = viewModel(factory = SummaryViewModel.factory(salesRepository(), date)),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SummaryScreen(
        uiState = uiState,
        onBack = onBack,
        onAddSales = onAddSales,
        onDateSelected = viewModel::selectDate,
        onUpdateSale = viewModel::updateSale,
        onDeleteSale = viewModel::deleteSale,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(
    uiState: SummaryUiState,
    onBack: () -> Unit,
    onAddSales: (LocalDate) -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onUpdateSale: (id: String, productName: String, priceCents: Long) -> Unit,
    onDeleteSale: (id: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var editingSaleId by rememberSaveable { mutableStateOf<String?>(null) }
    uiState.sales.find { it.id == editingSaleId }?.let { sale ->
        EditSaleDialog(
            sale = sale,
            onSave = { name, cents -> onUpdateSale(sale.id, name, cents) },
            onDismiss = { editingSaleId = null },
        )
    }
    var deletingSaleId by rememberSaveable { mutableStateOf<String?>(null) }
    uiState.sales.find { it.id == deletingSaleId }?.let { sale ->
        AlertDialog(
            onDismissRequest = { deletingSaleId = null },
            title = { Text(stringResource(R.string.delete_sale_title)) },
            text = { Text(stringResource(R.string.delete_sale_message, sale.productName)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteSale(sale.id)
                        deletingSaleId = null
                    },
                ) { Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { deletingSaleId = null }) { Text(stringResource(R.string.cancel)) }
            },
        )
    }
    if (showDatePicker) {
        SaleDatePickerDialog(
            initialDate = uiState.date,
            maxDate = uiState.today,
            onDateSelected = onDateSelected,
            onDismiss = { showDatePicker = false },
        )
    }
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.summary_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = stringResource(R.string.change_date),
                        )
                    }
                    IconButton(onClick = { onAddSales(uiState.date) }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = stringResource(R.string.add_sales_for_date),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        CenteredContentPadding(innerPadding) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = padding,
            ) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                        ),
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(stringResource(R.string.date), style = MaterialTheme.typography.labelLarge)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    localizedLongDate(uiState.date),
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.weight(1f),
                                )
                                if (!uiState.isToday) {
                                    TextButton(onClick = { onDateSelected(uiState.today) }) {
                                        Text(stringResource(R.string.today))
                                    }
                                }
                            }
                            Text(
                                stringResource(R.string.total_sold),
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.padding(top = 12.dp),
                            )
                            Text(
                                formatCents(uiState.totalCents),
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                stringResource(R.string.sales_count, uiState.count),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 12.dp),
                            )
                        }
                    }
                }
                if (uiState.sales.isEmpty()) {
                    item {
                        Text(
                            stringResource(R.string.no_sales_for_date),
                            modifier = Modifier.padding(horizontal = 16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
                items(uiState.sales, key = { it.id }) { sale ->
                    SwipeableSaleItem(
                        sale = sale,
                        onEdit = { editingSaleId = sale.id },
                        onDelete = { deletingSaleId = sale.id },
                        modifier = Modifier.animateItem(),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SummaryScreenPreview() {
    MyApplicationTheme {
        SummaryScreen(
            uiState = SummaryUiState(
                date = LocalDate.now(),
                totalCents = 12000,
                sales = listOf(Sale("1", "Pan", 12000, Instant.now())),
            ),
            onBack = {},
            onAddSales = {},
            onDateSelected = {},
            onUpdateSale = { _, _, _ -> },
            onDeleteSale = {},
        )
    }
}
