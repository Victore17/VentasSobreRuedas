package victor.mexico.ventassobreruedas.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import victor.mexico.ventassobreruedas.R
import victor.mexico.ventassobreruedas.ui.components.MaxFormWidth
import victor.mexico.ventassobreruedas.ui.theme.MyApplicationTheme
import victor.mexico.ventassobreruedas.ui.components.localizedLongDate
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userName: String?,
    onRegisterSales: () -> Unit,
    onViewSales: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val today = localizedLongDate(remember { LocalDate.now() })
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.home_title)) },
                actions = {
                    IconButton(onClick = onSignOut) {
                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = stringResource(R.string.sign_out),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        // Centrado vertical cuando cabe; con scroll cuando no (p. ej. teléfono en horizontal).
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .heightIn(min = maxHeight)
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    if (userName.isNullOrBlank()) {
                        stringResource(R.string.greeting_generic)
                    } else {
                        stringResource(R.string.greeting, userName)
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                )
                Text(
                    today,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(48.dp))
                Button(
                    onClick = onRegisterSales,
                    modifier = Modifier.homeButton(),
                ) {
                    ButtonContent(Icons.Default.Add, stringResource(R.string.register_sales_title))
                }
                Spacer(Modifier.height(16.dp))
                FilledTonalButton(
                    onClick = onViewSales,
                    modifier = Modifier.homeButton(),
                ) {
                    ButtonContent(Icons.Default.DateRange, stringResource(R.string.view_sales))
                }
            }
        }
    }
}

/** Ancho máximo en tablets; altura mínima (no fija) para que el texto no se corte con letra grande. */
private fun Modifier.homeButton() = widthIn(max = MaxFormWidth)
    .fillMaxWidth()
    .heightIn(min = 64.dp)

@Composable
private fun ButtonContent(icon: ImageVector, text: String) {
    Icon(icon, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
    Text(text, style = MaterialTheme.typography.titleMedium)
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    MyApplicationTheme {
        HomeScreen(userName = "Victor", onRegisterSales = {}, onViewSales = {}, onSignOut = {})
    }
}
