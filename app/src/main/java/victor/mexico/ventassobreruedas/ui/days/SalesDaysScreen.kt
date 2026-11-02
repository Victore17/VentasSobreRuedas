package victor.mexico.ventassobreruedas.ui.days

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import victor.mexico.ventassobreruedas.R
import victor.mexico.ventassobreruedas.data.repository.SalesDay
import victor.mexico.ventassobreruedas.salesRepository
import victor.mexico.ventassobreruedas.ui.theme.MyApplicationTheme
import victor.mexico.ventassobreruedas.util.formatCents
import victor.mexico.ventassobreruedas.util.formatLongDate
import java.time.LocalDate

@Composable
fun SalesDaysRoute(
    onBack: () -> Unit,
    onDayClick: (LocalDate) -> Unit,
    viewModel: SalesDaysViewModel = viewModel(factory = SalesDaysViewModel.factory(salesRepository())),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SalesDaysScreen(uiState = uiState, onBack = onBack, onDayClick = onDayClick)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesDaysScreen(
    uiState: SalesDaysUiState,
    onBack: () -> Unit,
    onDayClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.sales_days_title)) },
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
    ) { innerPadding ->
        when {
            uiState.isLoading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            uiState.days.isEmpty() -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    stringResource(R.string.no_sales_days),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = innerPadding,
            ) {
                items(uiState.days, key = { it.date.toEpochDay() }) { day ->
                    SalesDayItem(day = day, onClick = { onDayClick(day.date) })
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun SalesDayItem(day: SalesDay, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = { Text(formatLongDate(day.date), fontWeight = FontWeight.SemiBold) },
        supportingContent = {
            Text(
                pluralStringResource(R.plurals.sales_day_count, day.count, day.count),
                style = MaterialTheme.typography.bodySmall,
            )
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(formatCents(day.totalCents), style = MaterialTheme.typography.titleMedium)
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun SalesDaysScreenPreview() {
    val today = LocalDate.now()
    MyApplicationTheme {
        SalesDaysScreen(
            uiState = SalesDaysUiState(
                days = listOf(
                    SalesDay(today, 3, 45000),
                    SalesDay(today.minusDays(1), 1, 12000),
                ),
                isLoading = false,
            ),
            onBack = {},
            onDayClick = {},
        )
    }
}
