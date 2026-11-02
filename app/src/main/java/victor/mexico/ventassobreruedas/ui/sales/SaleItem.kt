package victor.mexico.ventassobreruedas.ui.sales

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import victor.mexico.ventassobreruedas.R
import victor.mexico.ventassobreruedas.data.repository.Sale
import victor.mexico.ventassobreruedas.util.formatCents
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

@Composable
fun SaleItem(
    sale: Sale,
    modifier: Modifier = Modifier,
    onDelete: (() -> Unit)? = null,
) {
    ListItem(
        modifier = modifier,
        headlineContent = { Text(sale.productName, fontWeight = FontWeight.SemiBold) },
        supportingContent = {
            Text(
                timeFormatter.format(sale.createdAt.atZone(ZoneId.systemDefault())),
                style = MaterialTheme.typography.labelSmall,
            )
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(formatCents(sale.priceCents), style = MaterialTheme.typography.titleMedium)
                if (onDelete != null) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete_sale))
                    }
                }
            }
        },
    )
}
