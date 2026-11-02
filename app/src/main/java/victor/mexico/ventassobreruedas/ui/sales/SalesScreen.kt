package victor.mexico.ventassobreruedas.ui.sales

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import victor.mexico.ventassobreruedas.R
import victor.mexico.ventassobreruedas.ui.components.CenteredContentPadding
import victor.mexico.ventassobreruedas.salesRepository
import victor.mexico.ventassobreruedas.data.repository.Sale
import victor.mexico.ventassobreruedas.ui.theme.MyApplicationTheme
import victor.mexico.ventassobreruedas.util.formatCents
import victor.mexico.ventassobreruedas.util.formatLongDate
import java.time.Instant
import java.time.LocalDate

@Composable
fun SalesRoute(
    date: LocalDate,
    onBack: () -> Unit,
    onFinishDay: () -> Unit,
    viewModel: SalesViewModel = viewModel(factory = SalesViewModel.factory(salesRepository(), date)),
) {
    val form by viewModel.form.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SalesScreen(
        form = form,
        uiState = uiState,
        onProductNameChange = viewModel::onProductNameChange,
        onPriceChange = viewModel::onPriceChange,
        onAddSale = viewModel::addSale,
        onDeleteSale = viewModel::deleteSale,
        onBack = onBack,
        onFinishDay = onFinishDay,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesScreen(
    form: SaleForm,
    uiState: SalesUiState,
    onProductNameChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onAddSale: () -> Boolean,
    onDeleteSale: (String) -> Unit,
    onBack: () -> Unit,
    onFinishDay: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Tras registrar una venta, el cursor vuelve a "Producto" para capturar la siguiente.
    val productFocus = remember { FocusRequester() }
    val submit = { if (onAddSale()) productFocus.requestFocus() }
    Scaffold(
        modifier = modifier.imePadding(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.register_sales_title))
                        if (!uiState.isToday) {
                            Text(
                                formatLongDate(uiState.date),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = onFinishDay) {
                Text(stringResource(R.string.finish_day))
            }
        },
    ) { innerPadding ->
        // Lista centrada en tablets; 88dp abajo para que el botón "Finalizar" no tape la última venta.
        CenteredContentPadding(innerPadding, extraBottom = 88.dp) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = padding,
            ) {
                item {
                    SaleFormCard(
                        form = form,
                        onProductNameChange = onProductNameChange,
                        onPriceChange = onPriceChange,
                        onAddSale = submit,
                        productFocus = productFocus,
                    )
                }
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            stringResource(R.string.sales_count, uiState.sales.size),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            formatCents(uiState.totalCents),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    HorizontalDivider()
                }
                if (uiState.sales.isEmpty()) {
                    item {
                        Text(
                            stringResource(
                                if (uiState.isToday) R.string.no_sales_yet else R.string.no_sales_for_date,
                            ),
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
                items(uiState.sales, key = { it.id }) { sale ->
                    SaleItem(
                        sale = sale,
                        onDelete = { onDeleteSale(sale.id) },
                        modifier = Modifier.animateItem(),
                    )
                }
            }
        }
    }
}

@Composable
private fun SaleFormCard(
    form: SaleForm,
    onProductNameChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onAddSale: () -> Unit,
    productFocus: FocusRequester,
) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedTextField(
            value = form.productName,
            onValueChange = onProductNameChange,
            label = { Text(stringResource(R.string.product)) },
            isError = form.productError,
            supportingText = if (form.productError) {
                { Text(stringResource(R.string.error_product)) }
            } else null,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Next,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(productFocus),
        )
        OutlinedTextField(
            value = form.price,
            onValueChange = onPriceChange,
            label = { Text(stringResource(R.string.price)) },
            isError = form.priceError,
            supportingText = if (form.priceError) {
                { Text(stringResource(R.string.error_price)) }
            } else null,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { onAddSale() }),
            modifier = Modifier.fillMaxWidth(),
        )
        Button(onClick = onAddSale, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.add_sale))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SalesScreenPreview() {
    MyApplicationTheme {
        SalesScreen(
            form = SaleForm(),
            uiState = SalesUiState(
                date = LocalDate.now(),
                sales = listOf(Sale("1", "Café", 3500, Instant.now())),
                totalCents = 3500,
            ),
            onProductNameChange = {},
            onPriceChange = {},
            onAddSale = { true },
            onDeleteSale = {},
            onBack = {},
            onFinishDay = {},
        )
    }
}
