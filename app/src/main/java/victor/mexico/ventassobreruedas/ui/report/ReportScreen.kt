package victor.mexico.ventassobreruedas.ui.report

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import victor.mexico.ventassobreruedas.R
import victor.mexico.ventassobreruedas.data.repository.PeriodSummary
import victor.mexico.ventassobreruedas.data.repository.Sale
import victor.mexico.ventassobreruedas.data.repository.buildPeriodSummary
import victor.mexico.ventassobreruedas.export.ReportPdf
import victor.mexico.ventassobreruedas.salesRepository
import victor.mexico.ventassobreruedas.ui.components.CenteredContentPadding
import victor.mexico.ventassobreruedas.ui.days.SalesDayItem
import victor.mexico.ventassobreruedas.ui.theme.MyApplicationTheme
import victor.mexico.ventassobreruedas.util.Period
import victor.mexico.ventassobreruedas.util.PeriodType
import victor.mexico.ventassobreruedas.util.changePercent
import victor.mexico.ventassobreruedas.util.formatCents
import victor.mexico.ventassobreruedas.util.formatCentsShort
import victor.mexico.ventassobreruedas.util.formatPeriodLabel
import victor.mexico.ventassobreruedas.util.formatShortDate
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

private const val TAG = "ReportScreen"

@Composable
fun ReportRoute(
    userName: String?,
    onBack: () -> Unit,
    onDayClick: (LocalDate) -> Unit,
    viewModel: ReportViewModel = viewModel(factory = ReportViewModel.factory(salesRepository())),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val zone = salesRepository().zone
    val scope = rememberCoroutineScope()
    var exporting by remember { mutableStateOf(false) }
    ReportScreen(
        uiState = uiState,
        exporting = exporting,
        onTypeSelected = viewModel::selectType,
        onPreviousPeriod = viewModel::previousPeriod,
        onNextPeriod = viewModel::nextPeriod,
        onDayClick = onDayClick,
        onBack = onBack,
        onSharePdf = share@{
            val summary = uiState.summary ?: return@share
            val previousTotal = uiState.previousTotalCents
            exporting = true
            scope.launch {
                try {
                    val file = withContext(Dispatchers.IO) {
                        ReportPdf.write(context, summary, previousTotal, userName, zone)
                    }
                    ReportPdf.share(context, file, ReportPdf.title(context, summary.period.type))
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e(TAG, "Error al generar el PDF", e)
                    Toast.makeText(context, R.string.pdf_error, Toast.LENGTH_LONG).show()
                } finally {
                    exporting = false
                }
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    uiState: ReportUiState,
    exporting: Boolean,
    onTypeSelected: (PeriodType) -> Unit,
    onPreviousPeriod: () -> Unit,
    onNextPeriod: () -> Unit,
    onDayClick: (LocalDate) -> Unit,
    onBack: () -> Unit,
    onSharePdf: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val locale = Locale.forLanguageTag(stringResource(R.string.date_locale))
    val summary = uiState.summary
    val weekly = uiState.period.type == PeriodType.WEEK
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.report_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
                actions = {
                    if (exporting) {
                        CircularProgressIndicator(modifier = Modifier.padding(12.dp).size(24.dp))
                    } else {
                        IconButton(onClick = onSharePdf, enabled = summary != null) {
                            Icon(Icons.Default.Share, contentDescription = stringResource(R.string.share_pdf))
                        }
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
                    PrimaryTabRow(selectedTabIndex = uiState.period.type.ordinal) {
                        PeriodType.entries.forEach { type ->
                            Tab(
                                selected = type == uiState.period.type,
                                onClick = { onTypeSelected(type) },
                                text = {
                                    Text(
                                        stringResource(
                                            if (type == PeriodType.WEEK) R.string.tab_week else R.string.tab_month,
                                        ),
                                    )
                                },
                            )
                        }
                    }
                }
                item {
                    PeriodSelector(
                        label = formatPeriodLabel(uiState.period, locale),
                        canGoNext = uiState.canGoNext,
                        onPrevious = onPreviousPeriod,
                        onNext = onNextPeriod,
                    )
                }
                if (summary == null) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(48.dp),
                            contentAlignment = Alignment.Center,
                        ) { CircularProgressIndicator() }
                    }
                } else {
                    item {
                        StatsGrid(summary, uiState.previousTotalCents, weekly, locale)
                    }
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            ),
                        ) {
                            SalesBarChart(
                                days = summary.days,
                                showValues = weekly,
                                locale = locale,
                                onDayClick = onDayClick,
                                modifier = Modifier.padding(12.dp),
                            )
                        }
                    }
                    if (summary.activeDays.isEmpty()) {
                        item {
                            Text(
                                stringResource(R.string.no_sales_in_period),
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    // Del día más reciente al más antiguo, igual que la lista de "Ver ventas".
                    items(summary.activeDays.reversed(), key = { it.date.toEpochDay() }) { day ->
                        SalesDayItem(day = day, onClick = { onDayClick(day.date) })
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun PeriodSelector(label: String, canGoNext: Boolean, onPrevious: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPrevious) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = stringResource(R.string.previous_period),
            )
        }
        Text(
            label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        IconButton(onClick = onNext, enabled = canGoNext) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = stringResource(R.string.next_period),
            )
        }
    }
}

@Composable
private fun StatsGrid(summary: PeriodSummary, previousTotalCents: Long, weekly: Boolean, locale: Locale) {
    val best = summary.bestDay
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard(stringResource(R.string.stat_total), formatCents(summary.totalCents), Modifier.weight(1f))
            StatCard(stringResource(R.string.stat_sales), summary.count.toString(), Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard(
                stringResource(R.string.stat_average),
                formatCents(summary.averagePerActiveDayCents),
                Modifier.weight(1f),
            )
            StatCard(
                stringResource(R.string.stat_best_day),
                best?.let { "${formatShortDate(it.date, locale)} · ${formatCentsShort(it.totalCents)}" } ?: "—",
                Modifier.weight(1f),
            )
        }
        changePercent(summary.totalCents, previousTotalCents)?.let { percent ->
            Text(
                stringResource(
                    if (weekly) R.string.vs_previous_week else R.string.vs_previous_month,
                    "%+d%%".format(percent),
                ),
                style = MaterialTheme.typography.labelLarge,
                color = if (percent >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 1,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ReportScreenPreview() {
    val today = LocalDate.now()
    val period = Period.containing(PeriodType.WEEK, today)
    val zone = ZoneId.systemDefault()
    val sales = period.days.filter { it <= today }.mapIndexed { i, day ->
        Sale("$i", "Café", 3500L * (i + 1), day.atTime(12, 0).atZone(zone).toInstant())
    }
    MyApplicationTheme {
        ReportScreen(
            uiState = ReportUiState(period, today, buildPeriodSummary(period, sales, zone), 20000),
            exporting = false,
            onTypeSelected = {},
            onPreviousPeriod = {},
            onNextPeriod = {},
            onDayClick = {},
            onBack = {},
            onSharePdf = {},
        )
    }
}
