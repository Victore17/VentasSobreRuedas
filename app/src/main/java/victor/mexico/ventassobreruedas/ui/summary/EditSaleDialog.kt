package victor.mexico.ventassobreruedas.ui.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import victor.mexico.ventassobreruedas.R
import victor.mexico.ventassobreruedas.data.repository.Sale
import victor.mexico.ventassobreruedas.util.parsePriceToCents
import java.math.BigDecimal

/** Diálogo para corregir el producto y el precio de una venta ya registrada. */
@Composable
fun EditSaleDialog(
    sale: Sale,
    onSave: (productName: String, priceCents: Long) -> Unit,
    onDismiss: () -> Unit,
) {
    var productName by rememberSaveable(sale.id) { mutableStateOf(sale.productName) }
    var price by rememberSaveable(sale.id) {
        mutableStateOf(BigDecimal.valueOf(sale.priceCents, 2).toPlainString())
    }
    var productError by rememberSaveable { mutableStateOf(false) }
    var priceError by rememberSaveable { mutableStateOf(false) }

    fun save() {
        val name = productName.trim()
        val cents = parsePriceToCents(price)
        productError = name.isEmpty()
        priceError = cents == null
        if (cents != null && name.isNotEmpty()) {
            onSave(name, cents)
            onDismiss()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.edit_sale)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = productName,
                    onValueChange = { productName = it; productError = false },
                    label = { Text(stringResource(R.string.product)) },
                    isError = productError,
                    supportingText = if (productError) {
                        { Text(stringResource(R.string.error_product)) }
                    } else null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it; priceError = false },
                    label = { Text(stringResource(R.string.price)) },
                    isError = priceError,
                    supportingText = if (priceError) {
                        { Text(stringResource(R.string.error_price)) }
                    } else null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = ::save) { Text(stringResource(R.string.save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )
}
