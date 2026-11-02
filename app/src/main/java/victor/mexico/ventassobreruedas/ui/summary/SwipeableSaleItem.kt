package victor.mexico.ventassobreruedas.ui.summary

import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import victor.mexico.ventassobreruedas.R
import victor.mexico.ventassobreruedas.data.repository.Sale
import victor.mexico.ventassobreruedas.ui.sales.SaleItem
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private enum class SwipeState { Closed, Open }

private val ActionWidth = 80.dp

/**
 * Renglón de venta que, al deslizarlo de derecha a izquierda, deja ver las acciones
 * "Editar" y "Eliminar". Las acciones también se exponen a TalkBack como acciones personalizadas.
 */
@Composable
fun SwipeableSaleItem(
    sale: Sale,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val revealPx = with(LocalDensity.current) { (ActionWidth * 2).toPx() }
    val state = remember(revealPx) {
        AnchoredDraggableState(
            initialValue = SwipeState.Closed,
            anchors = DraggableAnchors {
                SwipeState.Closed at 0f
                SwipeState.Open at -revealPx
            },
        )
    }
    val scope = rememberCoroutineScope()
    fun closeThen(action: () -> Unit) {
        action()
        scope.launch { state.animateTo(SwipeState.Closed) }
    }

    val editLabel = stringResource(R.string.edit)
    val deleteLabel = stringResource(R.string.delete)
    Box(
        modifier = modifier.semantics {
            customActions = listOf(
                CustomAccessibilityAction(editLabel) { onEdit(); true },
                CustomAccessibilityAction(deleteLabel) { onDelete(); true },
            )
        },
    ) {
        Row(
            modifier = Modifier
                .matchParentSize(),
            horizontalArrangement = Arrangement.End,
        ) {
            SwipeAction(
                icon = Icons.Default.Edit,
                label = editLabel,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                onClick = { closeThen(onEdit) },
            )
            SwipeAction(
                icon = Icons.Default.Delete,
                label = deleteLabel,
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                onClick = { closeThen(onDelete) },
            )
        }
        SaleItem(
            sale = sale,
            modifier = Modifier
                .offset { IntOffset(state.requireOffset().roundToInt(), 0) }
                .anchoredDraggable(state, Orientation.Horizontal),
        )
    }
}

@Composable
private fun SwipeAction(
    icon: ImageVector,
    label: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(ActionWidth)
            .fillMaxHeight()
            .background(containerColor)
            .clickable(onClick = onClick),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, contentDescription = null, tint = contentColor)
        Text(label, color = contentColor, style = MaterialTheme.typography.labelMedium)
    }
}
